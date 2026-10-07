package com.standbyus.app.data.repository

import android.content.Context
import android.util.Log
import com.standbyus.app.data.local.CheckinDao
import com.standbyus.app.data.local.toData
import com.standbyus.app.data.local.toEntity
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import com.standbyus.app.data.model.CheckinDates
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

@Singleton
class CheckinRepository @Inject constructor(
    private val supabaseService: SupabaseService,
    private val checkinDao: CheckinDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CheckinRepo"
        private const val TABLE = "checkins"
        private const val POLL_MS = 6_000L
    }


    private val userLocks = ConcurrentHashMap<String, Mutex>()
    private fun userLock(userId: String): Mutex = userLocks.getOrPut(userId) { Mutex() }
    private val _syncErrors = MutableStateFlow<Map<String, CheckinFailure>>(emptyMap())
    val syncErrors = _syncErrors.asStateFlow()

    suspend fun submitCheckIn(note: String = ""): CheckinSubmitResult {
        val userId = supabaseService.getDeviceId(context)
        val now = System.currentTimeMillis()
        val data = CheckinData(userId = userId, timestamp = now, note = note)
        return userLock(userId).withLock {
            persistCheckin(
                record = data,
                create = { supabaseService.create(TABLE, it.toMap())?.let(CheckinData::fromMap) },
                cache = { checkinDao.insert(it.toEntity()) }
            )
        }
    }

    suspend fun getTodayCount(userId: String): Int {
        return checkinDao.getTodayCount(userId, getStartOfDay())
    }

    suspend fun getFirstCheckinTime(userId: String): Long? {
        return checkinDao.getFirstCheckinTime(userId, getStartOfDay())
    }

    suspend fun getLastCheckinTime(userId: String): Long? {
        return checkinDao.getLastCheckinTime(userId)
    }

    suspend fun getWeekCount(userId: String): Int {
        return checkinDao.getWeekCount(userId, getStartOfWeek())
    }

    suspend fun getMonthCount(userId: String): Int {
        return checkinDao.getMonthCount(userId, getStartOfMonth())
    }

    fun observeCheckIns(userId: String): Flow<List<CheckinData>> = flow {
        emit(checkinDao.getRecordsSince(userId, 0L).map { it.toData() })
        var needsFullSync = true
        while (currentCoroutineContext().isActive) {
            try {
                // Serialize a user's snapshot with writes so an older poll cannot erase a new check-in.
                userLock(userId).withLock {
                    // First load includes older days so streaks survive week/month boundaries.
                    // Later polls replace only the current statistics window, preserving older days.
                    val since = if (needsFullSync) 0L else minOf(getStartOfWeek(), getStartOfMonth())
                    val records = mutableListOf<CheckinData>()
                    var cursor = 0L
                    do {
                        val page = supabaseService.query(TABLE,
                            "userId=eq.$userId&timestamp=gte.$since&id=gt.$cursor&order=id.asc&limit=500")
                            .map(CheckinData::fromMap)
                        records += page
                        if (page.isNotEmpty()) {
                            check(page.last().id > cursor)
                            cursor = page.last().id
                        }
                    } while (page.size == 500)
                    checkinDao.replaceRecordsSince(userId, since, records.map { it.toEntity() })
                    needsFullSync = false
                }
                _syncErrors.update { it - userId }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "Check-in sync failed; keeping cache", e)
                _syncErrors.update { it + (userId to CheckinFailure.from(e)) }
            }
            emit(checkinDao.getRecordsSince(userId, 0L).map { it.toData() })
            delay(POLL_MS)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getStreakDays(userId: String): Int = CheckinDates.streak(
        checkinDao.getTimestamps(userId), LocalDate.now(), ZoneId.systemDefault()
    )

    private fun getStartOfDay(): Long = CheckinDates.startOfDay(LocalDate.now(), ZoneId.systemDefault())
    private fun getStartOfWeek(): Long = CheckinDates.startOfWeek(LocalDate.now(), ZoneId.systemDefault())
    private fun getStartOfMonth(): Long = CheckinDates.startOfMonth(LocalDate.now(), ZoneId.systemDefault())
}
