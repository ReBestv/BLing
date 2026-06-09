package com.standbyus.app.ui.album

import com.standbyus.app.data.model.AlbumPhoto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class AlbumDaySection(
    val date: LocalDate,
    val title: String,
    val photos: List<AlbumPhoto>
)

private val albumDateLocale = Locale.CHINA
private val albumSameYearDateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", albumDateLocale)
private val albumCrossYearDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", albumDateLocale)
private val albumFullDateFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm", albumDateLocale)

internal fun albumDaySections(
    photos: List<AlbumPhoto>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<AlbumDaySection> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

    return photos
        .sortedByDescending { it.createdAt }
        .groupBy { it.createdAt.toAlbumLocalDate(zoneId) }
        .map { (date, dayPhotos) ->
            AlbumDaySection(
                date = date,
                title = albumDateHeaderLabel(date, today),
                photos = dayPhotos
            )
        }
}

internal fun albumDateHeaderLabel(
    date: LocalDate,
    today: LocalDate = LocalDate.now()
): String {
    val dateText = if (date.year == today.year) {
        date.format(albumSameYearDateFormatter)
    } else {
        date.format(albumCrossYearDateFormatter)
    }

    return when (date) {
        today -> "今天 · $dateText"
        today.minusDays(1) -> "昨天 · $dateText"
        else -> dateText
    }
}

internal fun albumUploadedAtLabel(
    createdAt: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    val dateTime = Instant.ofEpochMilli(createdAt).atZone(zoneId).toLocalDateTime()
    return "上传于 ${dateTime.format(albumFullDateFormatter)}"
}

private fun Long.toAlbumLocalDate(zoneId: ZoneId): LocalDate =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
