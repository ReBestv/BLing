package com.standbyus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.standbyus.app.data.model.UserStatus

@Entity(tableName = "status_cache")
data class StatusEntity(
    @PrimaryKey val userId: String,
    val doing: String,
    val customDoing: String,
    val feeling: String,
    val feelingColor: String,
    val feelingEmoji: String,
    val note: String,
    val updatedAt: Long,
    val source: String
)

fun UserStatus.toEntity() = StatusEntity(
    userId = userId,
    doing = doing,
    customDoing = customDoing,
    feeling = feeling,
    feelingColor = feelingColor,
    feelingEmoji = feelingEmoji,
    note = note,
    updatedAt = updatedAt,
    source = source
)

fun StatusEntity.toUserStatus() = UserStatus(
    userId = userId,
    doing = doing,
    customDoing = customDoing,
    feeling = feeling,
    feelingColor = feelingColor,
    feelingEmoji = feelingEmoji,
    note = note,
    updatedAt = updatedAt,
    source = source
)
