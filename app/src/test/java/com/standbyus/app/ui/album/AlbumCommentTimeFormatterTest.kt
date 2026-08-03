package com.standbyus.app.ui.album

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumCommentTimeFormatterTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val now = millisOf(2026, 8, 3, 20, 0)

    @Test
    fun `uses relative labels for recent comments`() {
        assertEquals("刚刚", albumCommentTimeLabel(now - 45_000L, now, zoneId))
        assertEquals("8 分钟前", albumCommentTimeLabel(now - 8 * 60_000L, now, zoneId))
        assertEquals("3 小时前", albumCommentTimeLabel(now - 3 * 60 * 60_000L, now, zoneId))
    }

    @Test
    fun `uses month and day for older comments`() {
        val createdAt = millisOf(2026, 7, 31, 18, 30)

        assertEquals("7月31日", albumCommentTimeLabel(createdAt, now, zoneId))
    }

    private fun millisOf(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ): Long = LocalDateTime.of(year, month, day, hour, minute)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}
