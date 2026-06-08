package com.standbyus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.standbyus.app.data.model.TodoItem

@Entity(tableName = "todo_items")
data class TodoItemEntity(
    @PrimaryKey val id: Long = 0,
    val listId: Long = 0,
    val title: String = "",
    val isDone: Boolean = false,
    val note: String = "",
    val ownerId: String = "",
    val doneBy: String? = null,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val doneAt: Long? = null
)

fun TodoItem.toEntity() = TodoItemEntity(
    id = id,
    listId = listId,
    title = title,
    isDone = isDone,
    note = note,
    ownerId = ownerId,
    doneBy = doneBy,
    sortOrder = sortOrder,
    createdAt = createdAt,
    doneAt = doneAt
)

fun TodoItemEntity.toTodoItem() = TodoItem(
    id = id,
    listId = listId,
    title = title,
    isDone = isDone,
    note = note,
    ownerId = ownerId,
    doneBy = doneBy,
    sortOrder = sortOrder,
    createdAt = createdAt,
    doneAt = doneAt
)