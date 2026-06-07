package com.standbyus.app.ui.settings

import com.standbyus.app.ui.theme.StatusEmoji

object SettingsDisplayName {
    fun resolvePartnerDisplayName(
        nickname: String?,
        partnerName: String?
    ): String {
        return listOf(nickname, partnerName)
            .map { it.orEmpty().trim() }
            .firstOrNull { it.isNotEmpty() && !looksLikeAssetPath(it) }
            ?: "对方"
    }

    private fun looksLikeAssetPath(value: String): Boolean {
        val lower = value.lowercase()
        return StatusEmoji.isRemoteAsset(value) ||
            lower.contains("/storage/") ||
            lower.endsWith(".png") ||
            lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".webp") ||
            lower.endsWith(".gif") ||
            lower.endsWith(".heif") ||
            lower.endsWith(".heic") ||
            lower.endsWith(".mov") ||
            lower.endsWith(".mp4") ||
            lower.endsWith(".m4v") ||
            lower.endsWith(".webm")
    }
}
