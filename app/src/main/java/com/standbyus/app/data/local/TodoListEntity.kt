package com.standbyus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.standbyus.app.data.model.TodoList

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey val id: Long = 0,
    val pairId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val isShared: Boolean = false,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

fun TodoList.toEntity() = TodoListEntity(
    id = id,
    pairId = pairId,
    name = name,
    ownerId = ownerId,
    isShared = isShared,
    sortOrder = sortOrder,
    createdAt = createdAt
)

fun TodoListEntity.toTodoList() = TodoList(
    id = id,
    pairId = pairId,
    name = name,
    ownerId = ownerId,
    isShared = isShared,
    sortOrder = sortOrder,
    createdAt = createdAt
)