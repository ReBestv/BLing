package com.standbyus.app.data.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object CheckinDates {
    fun startOfDay(date: LocalDate, zone: ZoneId): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()
    fun startOfWeek(date: LocalDate, zone: ZoneId): Long =
        startOfDay(date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), zone)
    fun startOfMonth(date: LocalDate, zone: ZoneId): Long = startOfDay(date.withDayOfMonth(1), zone)

    fun streak(timestamps: List<Long>, today: LocalDate, zone: ZoneId): Int {
        val days = timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.toSet()
        // Yesterday's streak stays alive until the current local day has ended.
        var day = if (today in days) today else today.minusDays(1)
        var count = 0
        while (day in days) {
            count++
            day = day.minusDays(1)
        }
        return count
    }
}
