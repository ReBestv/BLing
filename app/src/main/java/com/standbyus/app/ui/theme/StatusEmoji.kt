package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.Feeling

object StatusEmoji {
    fun isRemoteImage(value: String?): Boolean {
        val lower = value.orEmpty().trim().lowercase()
        return (lower.startsWith("http://") || lower.startsWith("https://")) &&
            (lower.endsWith(".png") ||
                lower.endsWith(".jpg") ||
                lower.endsWith(".jpeg") ||
                lower.endsWith(".webp"))
    }

    fun textFallback(value: String?, feelingKeyOrLabel: String?, fallbackEmoji: String? = null): String {
        val raw = value.orEmpty().trim()
        if (raw.isNotEmpty() && !isRemoteImage(raw)) return raw
        return fallbackEmoji
            ?: Feeling.fromKey(feelingKeyOrLabel.orEmpty())?.emoji
            ?: Feeling.fromLegacyLabel(feelingKeyOrLabel.orEmpty())?.emoji
            ?: "✨"
    }
}
