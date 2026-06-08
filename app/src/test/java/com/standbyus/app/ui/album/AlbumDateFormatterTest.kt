package com.standbyus.app.ui.album

import com.standbyus.app.data.model.AlbumPhoto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumDateFormatterTest {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val today = LocalDate.of(2026, 6, 7)

    @Test
    fun `formats friendly date headers`() {
        assertEquals(
            "今天 · 6月7日 星期日",
            albumDateHeaderLabel(LocalDate.of(2026, 6, 7), today)
        )
        assertEquals(
            "昨天 · 6月6日 星期六",
            albumDateHeaderLabel(LocalDate.of(2026, 6, 6), today)
        )
        assertEquals(
            "2025年12月24日 星期三",
            albumDateHeaderLabel(LocalDate.of(2025, 12, 24), today)
        )
    }

    @Test
    fun `formats full upload timestamp`() {
        val createdAt = millisOf(2026, 6, 7, 22, 18)

        assertEquals(
            "上传于 2026年6月7日 22:18",
            albumUploadedAtLabel(createdAt, zoneId)
        )
    }

    @Test
    fun `groups photos by upload day in newest first order`() {
        val photos = listOf(
            AlbumPhoto(id = 1, createdAt = millisOf(2026, 6, 6, 19, 0)),
            AlbumPhoto(id = 2, createdAt = millisOf(2026, 6, 7, 8, 30)),
            AlbumPhoto(id = 3, createdAt = millisOf(2026, 6, 7, 22, 18))
        )

        val sections = albumDaySections(
            photos = photos,
            nowMillis = millisOf(2026, 6, 7, 23, 0),
            zoneId = zoneId
        )

        assertEquals(listOf("今天 · 6月7日 星期日", "昨天 · 6月6日 星期六"), sections.map { it.title })
        assertEquals(listOf(3L, 2L), sections.first().photos.map { it.id })
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
