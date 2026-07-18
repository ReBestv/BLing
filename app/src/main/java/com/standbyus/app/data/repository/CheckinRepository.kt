package com.standbyus.app.data.repository

import android.content.Context
import android.util.Log
import com.standbyus.app.data.local.CheckinDao
import com.standbyus.app.data.local.toData
import com.standbyus.app.data.local.toEntity
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun submitCheckIn(note: String = ""): Boolean {
        val userId = supabaseService.getCachedDeviceId()
        if (userId.isEmpty()) {
            Log.e(TAG, "submitCheckIn: no device ID")
            return false
        }
        val now = System.currentTimeMillis()
        val data = CheckinData(userId = userId, timestamp = now, note = note)
        return try {
            supabaseService.create(TABLE, data.toMap())
            checkinDao.insert(data.toEntity())
            true
        } catch (e: Exception) {
            Log.e(TAG, "submitCheckIn failed: ${e.message}", e)
            false
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

    fun observeCheckIns(userId: String, since: Long): Flow<List<CheckinData>> = callbackFlow {
        suspend fun fetchFromRemote() {
            try {
                val results = supabaseService.query(
                    TABLE,
                    "userId=eq.$userId&timestamp=gte.$since&order=timestamp.desc"
                )
                val records = results.map { CheckinData.fromMap(it) }

                // Replace only the fetched range so historical month caches remain available.
                // Unpair cleanup still clears every row for the affected user explicitly.
                checkinDao.clearUserRecordsSince(userId, since)
                records.forEach { checkinDao.insert(it.toEntity()) }
                trySend(records)
            } catch (e: Exception) {
                Log.e(TAG, "observeCheckIns remote failed: ${e.message}")
                val cached = checkinDao.getRecordsSince(userId, since)
                trySend(cached.map { it.toData() })
            }
        }

        fetchFromRemote()

        val job = widgetScope.launch {
            delay(POLL_MS)
            while (isActive) {
                fetchFromRemote()
                delay(POLL_MS)
            }
        }
        awaitClose { job.cancel() }
    }

    suspend fun getCheckInsInRange(
        userId: String,
        startInclusive: Long,
        endExclusive: Long
    ): List<CheckinData> {
        if (userId.isEmpty()) return emptyList()

        return try {
            val results = supabaseService.query(
                TABLE,
                "userId=eq.$userId" +
                    "&timestamp=gte.$startInclusive" +
                    "&timestamp=lt.$endExclusive" +
                    "&order=timestamp.desc"
            )
            val records = results.map { CheckinData.fromMap(it) }
            records.forEach { checkinDao.insert(it.toEntity()) }
            records
        } catch (e: Exception) {
            Log.e(TAG, "getCheckInsInRange remote failed: ${e.message}")
            checkinDao.getRecordsInRange(userId, startInclusive, endExclusive)
                .map { it.toData() }
        }
    }

    suspend fun getStreakDays(userId: String): Int {
        val activeDays = checkinDao.getActiveDays(userId, getStartOfWeek())
        if (activeDays.isEmpty()) return 0

        val todayEpochDay = System.currentTimeMillis() / 86400000
        val sorted = activeDays.sortedDescending()
        var streak = 0
        for (i in sorted.indices) {
            if (sorted[i] == todayEpochDay - i) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
