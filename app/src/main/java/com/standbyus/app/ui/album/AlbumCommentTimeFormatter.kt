package com.standbyus.app.ui.album

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun albumCommentTimeLabel(
    createdAt: Long,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    val elapsedMillis = (nowMillis - createdAt).coerceAtLeast(0L)
    return when {
        elapsedMillis < MINUTE_MILLIS -> "刚刚"
        elapsedMillis < HOUR_MILLIS -> "${elapsedMillis / MINUTE_MILLIS} 分钟前"
        elapsedMillis < DAY_MILLIS -> "${elapsedMillis / HOUR_MILLIS} 小时前"
        else -> Instant.ofEpochMilli(createdAt)
            .atZone(zoneId)
            .format(DateTimeFormatter.ofPattern("M月d日"))
    }
}

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS
