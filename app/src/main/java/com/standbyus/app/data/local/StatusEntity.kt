package com.standbyus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.standbyus.app.data.model.UserStatus

@Entity(tableName = "status_cache")
data class StatusEntity(
    @PrimaryKey val userId: String,
    val doing: String,
    val customDoing: String,
    val themeId: String,
    val themeName: String,
    val feelingKey: String,
    val feelingLabel: String,
    val feelingAsset: String,
    val feelingFallbackEmoji: String,
    val feelingColor: String,
    val stickerId: String?,
    val stickerLabel: String?,
    val stickerAsset: String?,
    val note: String,
    val updatedAt: Long,
    val source: String
)

fun UserStatus.toEntity() = StatusEntity(
    userId = userId,
    doing = doing,
    customDoing = customDoing,
    themeId = themeId,
    themeName = themeName,
    feelingKey = feelingKey,
    feelingLabel = feelingLabel,
    feelingAsset = feelingAsset,
    feelingFallbackEmoji = feelingFallbackEmoji,
    feelingColor = feelingColor,
    stickerId = stickerId,
    stickerLabel = stickerLabel,
    stickerAsset = stickerAsset,
    note = note,
    updatedAt = updatedAt,
    source = source
)

fun StatusEntity.toUserStatus() = UserStatus(
    userId = userId,
    doing = doing,
    customDoing = customDoing,
    themeId = themeId,
    themeName = themeName,
    feelingKey = feelingKey,
    feelingLabel = feelingLabel,
    feelingAsset = feelingAsset,
    feelingFallbackEmoji = feelingFallbackEmoji,
    feelingColor = feelingColor,
    stickerId = stickerId,
    stickerLabel = stickerLabel,
    stickerAsset = stickerAsset,
    note = note,
    updatedAt = updatedAt,
    source = source
)
