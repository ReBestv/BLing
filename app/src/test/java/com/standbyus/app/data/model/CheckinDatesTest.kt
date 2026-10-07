package com.standbyus.app.data.model

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckinDatesTest {
    private val zone = ZoneId.of("Asia/Shanghai")
    private fun timestamp(value: String) = ZonedDateTime.parse(value).toInstant().toEpochMilli()

    @Test fun countsLocalMidnightAsTwoDays() {
        val records = listOf(timestamp("2026-10-01T23:00:00+08:00"), timestamp("2026-10-02T01:00:00+08:00"))
        assertEquals(2, CheckinDates.streak(records, LocalDate.of(2026, 10, 2), zone))
    }

    @Test fun keepsStreakAcrossWeeksMonthsAndYear() {
        val today = LocalDate.of(2027, 1, 2)
        val records = (0L..39L).map { CheckinDates.startOfDay(today.minusDays(it), zone) }
        assertEquals(40, CheckinDates.streak(records, today, zone))
    }

    @Test fun ignoresDuplicateCheckinsAndStopsAtMissingDay() {
        val today = LocalDate.of(2026, 10, 2)
        val records = listOf(0L, 0L, 1L, 3L).map { CheckinDates.startOfDay(today.minusDays(it), zone) }
        assertEquals(2, CheckinDates.streak(records, today, zone))
    }

    @Test fun yesterdayStaysActiveUntilTodayEnds() {
        val today = LocalDate.of(2026, 10, 2)
        val records = listOf(CheckinDates.startOfDay(today.minusDays(1), zone))
        assertEquals(1, CheckinDates.streak(records, today, zone))
        assertEquals(0, CheckinDates.streak(records, today.plusDays(1), zone))
    }

    @Test fun weekStartsOnMondayEvenWhenMonthStartsMidweek() {
        val date = LocalDate.of(2026, 10, 2)
        assertEquals(CheckinDates.startOfDay(LocalDate.of(2026, 9, 28), zone), CheckinDates.startOfWeek(date, zone))
        assertEquals(CheckinDates.startOfDay(LocalDate.of(2026, 10, 1), zone), CheckinDates.startOfMonth(date, zone))
    }

    @Test fun daylightSavingDoesNotChangeDayCount() {
        val newYork = ZoneId.of("America/New_York")
        val today = LocalDate.of(2026, 3, 9)
        val records = (0L..2L).map { CheckinDates.startOfDay(today.minusDays(it), newYork) }
        assertEquals(3, CheckinDates.streak(records, today, newYork))
    }
}
