package com.standbyus.app.ui.checkin

object CheckinIntervalFormatter {
    fun sinceLastCheckin(
        lastCheckinTime: Long?,
        now: Long
    ): String {
        if (lastCheckinTime == null) return "—"

        val diff = (now - lastCheckinTime).coerceAtLeast(0L)
        val hours = diff / 3_600_000L
        val minutes = (diff % 3_600_000L) / 60_000L
        return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
    }
}
