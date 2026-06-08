package com.standbyus.app.ui.history

object HistoryDisplayName {
    fun resolveEntryDisplayName(
        isMe: Boolean,
        partnerDisplayName: String?
    ): String {
        if (isMe) return "我"

        return partnerDisplayName.orEmpty().trim().ifEmpty { "对方" }
    }
}
