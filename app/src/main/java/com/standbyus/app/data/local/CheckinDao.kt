package com.standbyus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CheckinDao {
    @Query("SELECT COUNT(*) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfDay")
    suspend fun getTodayCount(userId: String, startOfDay: Long): Int

    @Query("SELECT MIN(timestamp) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfDay")
    suspend fun getFirstCheckinTime(userId: String, startOfDay: Long): Long?

    @Query("SELECT MAX(timestamp) FROM checkin_records WHERE userId = :userId")
    suspend fun getLastCheckinTime(userId: String): Long?

    @Query("SELECT COUNT(*) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfWeek")
    suspend fun getWeekCount(userId: String, startOfWeek: Long): Int

    @Query("SELECT COUNT(*) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfMonth")
    suspend fun getMonthCount(userId: String, startOfMonth: Long): Int

    /** 获取从 since 时间以来的所有活跃日期（以天为单位），用于计算连续打卡 */
    @Query("SELECT DISTINCT CAST(timestamp / 86400000 AS INTEGER) FROM checkin_records " +
           "WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getActiveDays(userId: String, since: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: CheckinRecordEntity)

    @Query("DELETE FROM checkin_records WHERE userId = :userId")
    suspend fun clearUserRecords(userId: String)

    @Query("DELETE FROM checkin_records WHERE userId = :userId AND timestamp >= :since")
    suspend fun clearUserRecordsSince(userId: String, since: Long)

    /** 获取本地缓存的记录列表（供轮询 fallback 使用） */
    @Query("SELECT * FROM checkin_records WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecordsSince(userId: String, since: Long): List<CheckinRecordEntity>

    /** 获取指定时间范围内的记录，用于月历和离线回退。 */
    @Query(
        "SELECT * FROM checkin_records " +
            "WHERE userId = :userId AND timestamp >= :startInclusive AND timestamp < :endExclusive " +
            "ORDER BY timestamp DESC"
    )
    suspend fun getRecordsInRange(
        userId: String,
        startInclusive: Long,
        endExclusive: Long
    ): List<CheckinRecordEntity>
}
