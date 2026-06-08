package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.Feeling

object StatusEmoji {
    private val remoteImageExtensions = setOf(
        ".png",
        ".jpg",
        ".jpeg",
        ".webp",
        ".gif",
        ".heif",
        ".heic"
    )

    private val remoteAssetExtensions = remoteImageExtensions + setOf(
        ".mov",
        ".mp4",
        ".m4v",
        ".webm"
    )

    fun isRemoteImage(value: String?): Boolean {
        val normalized = normalizeUrl(value)
        return isHttpUrl(normalized) && remoteImageExtensions.any(normalized::endsWith)
    }

    fun isRemoteAsset(value: String?): Boolean {
        val normalized = normalizeUrl(value)
        return isHttpUrl(normalized) && remoteAssetExtensions.any(normalized::endsWith)
    }

    fun textFallback(value: String?, feelingKeyOrLabel: String?, fallbackEmoji: String? = null): String {
        val raw = value.orEmpty().trim()
        if (raw.isNotEmpty() && !isRemoteAsset(raw)) return raw
        return fallbackEmoji
            ?: Feeling.fromKey(feelingKeyOrLabel.orEmpty())?.emoji
            ?: Feeling.fromLegacyLabel(feelingKeyOrLabel.orEmpty())?.emoji
            ?: "✨"
    }

    private fun normalizeUrl(value: String?): String {
        return value.orEmpty()
            .trim()
            .substringBefore('?')
            .substringBefore('#')
            .lowercase()
    }

    private fun isHttpUrl(value: String): Boolean {
        return value.startsWith("http://") || value.startsWith("https://")
    }
}
