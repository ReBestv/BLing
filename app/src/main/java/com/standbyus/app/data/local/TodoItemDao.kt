package com.standbyus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TodoItemDao {
    @Query("SELECT * FROM todo_items WHERE listId = :listId ORDER BY sortOrder DESC")
    suspend fun getItemsByListId(listId: Long): List<TodoItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: TodoItemEntity)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM todo_items")
    suspend fun clearAll()
}
