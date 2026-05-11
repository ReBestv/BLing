package com.standbyus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatusDao {
    @Query("SELECT * FROM status_cache WHERE userId = :userId")
    suspend fun getStatus(userId: String): StatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatus(status: StatusEntity)

    @Query("DELETE FROM status_cache")
    suspend fun clearAll()
}
