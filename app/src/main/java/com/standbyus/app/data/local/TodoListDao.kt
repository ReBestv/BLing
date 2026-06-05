package com.standbyus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TodoListDao {
    @Query("SELECT * FROM todo_lists WHERE pairId = :pairId ORDER BY sortOrder DESC")
    suspend fun getListsByPairId(pairId: String): List<TodoListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(list: TodoListEntity)

    @Query("DELETE FROM todo_lists WHERE id = :id")
    suspend fun deleteList(id: Long)
}