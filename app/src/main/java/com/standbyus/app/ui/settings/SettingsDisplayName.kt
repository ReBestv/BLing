package com.standbyus.app.ui.settings

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
        return lower.startsWith("http://") ||
            lower.startsWith("https://") ||
            lower.contains("/storage/") ||
            lower.endsWith(".png") ||
            lower.endsWith(".jpg") ||
            lower.endsWith(".jpeg") ||
            lower.endsWith(".webp")
    }
}
