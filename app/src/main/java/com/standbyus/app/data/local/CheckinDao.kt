package com.standbyus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

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

    @Query("SELECT timestamp FROM checkin_records WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getTimestamps(userId: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<CheckinRecordEntity>)

    @Query("DELETE FROM checkin_records WHERE userId = :userId AND timestamp >= :since")
    suspend fun clearRecordsSince(userId: String, since: Long)

    @Transaction
    suspend fun replaceRecordsSince(userId: String, since: Long, records: List<CheckinRecordEntity>) {
        clearRecordsSince(userId, since)
        insertAll(records)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: CheckinRecordEntity)

    @Query("DELETE FROM checkin_records WHERE userId = :userId")
    suspend fun clearUserRecords(userId: String)

    /** 获取本地缓存的记录列表（供轮询 fallback 使用） */
    @Query("SELECT * FROM checkin_records WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecordsSince(userId: String, since: Long): List<CheckinRecordEntity>
}
