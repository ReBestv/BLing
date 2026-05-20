package com.standbyus.app.data.repository

import android.content.Context
import android.util.Log
import com.standbyus.app.data.local.CheckinDao
import com.standbyus.app.data.local.toData
import com.standbyus.app.data.local.toEntity
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

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

    /** 提交一条打卡记录 */
    suspend fun submitCheckIn(note: String = ""): Boolean {
        val userId = supabaseService.getCachedDeviceId()
        if (userId.isEmpty()) {
            Log.e(TAG, "submitCheckIn: no device ID")
            return false
        }
        val now = System.currentTimeMillis()
        val data = CheckinData(userId = userId, timestamp = now, note = note)
        return try {
            // ① 写入 Supabase
            supabaseService.create(TABLE, data.toMap())
            Log.d(TAG, "Supabase create success")
            // ② Room 缓存
            checkinDao.insert(data.toEntity())
            Log.d(TAG, "Room insert success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "submitCheckIn failed: ${e.message}", e)
            false
        }
    }

    /** 获取今日打卡次数 */
    suspend fun getTodayCount(userId: String): Int {
        val startOfDay = getStartOfDay()
        return checkinDao.getTodayCount(userId, startOfDay)
    }

    /** 获取今日首次打卡时间 */
    suspend fun getFirstCheckinTime(userId: String): Long? {
        val startOfDay = getStartOfDay()
        return checkinDao.getFirstCheckinTime(userId, startOfDay)
    }

    /** 获取上次打卡时间 */
    suspend fun getLastCheckinTime(userId: String): Long? {
        return checkinDao.getLastCheckinTime(userId)
    }

    /** 获取本周打卡次数 */
    suspend fun getWeekCount(userId: String): Int {
        val startOfWeek = getStartOfWeek()
        return checkinDao.getWeekCount(userId, startOfWeek)
    }

    /** 获取本月打卡次数 */
    suspend fun getMonthCount(userId: String): Int {
        val startOfMonth = getStartOfMonth()
        return checkinDao.getMonthCount(userId, startOfMonth)
    }

    /** 轮询观察某用户的打卡记录 */
    fun observeCheckIns(userId: String, since: Long): Flow<List<CheckinData>> = callbackFlow {
        suspend fun fetchFromRemote() {
            try {
                val results = supabaseService.query(
                    TABLE,
                    "userId=eq.$userId&timestamp=gte.$since&order=timestamp.desc"
                )
                if (results.isNotEmpty()) {
                    val records = results.map { CheckinData.fromMap(it) }
                    // 缓存到 Room
                    records.forEach { checkinDao.insert(it.toEntity()) }
                    trySend(records)
                } else {
                    // 远程无数据，尝试本地缓存
                    val cached = checkinDao.getRecordsSince(userId, since)
                    if (cached.isNotEmpty()) trySend(cached.map { it.toData() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "observeCheckIns remote failed: ${e.message}")
                // fallback 到 Room 缓存
                val cached = checkinDao.getRecordsSince(userId, since)
                if (cached.isNotEmpty()) trySend(cached.map { it.toData() })
            }
        }

        // 首次立即执行
        fetchFromRemote()

        // 定时轮询
        val job = widgetScope.launch {
            delay(POLL_MS)
            while (isActive) {
                fetchFromRemote()
                delay(POLL_MS)
            }
        }
        awaitClose { job.cancel() }
    }

    /** 计算连续打卡天数 */
    suspend fun getStreakDays(userId: String): Int {
        val startOfWeek = getStartOfWeek()
        val activeDays = checkinDao.getActiveDays(userId, startOfWeek)
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

    // ── 时间工具 ──

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
