package com.standbyus.app.ui.checkin

import com.standbyus.app.data.model.CheckinData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CheckinCalendarModelsTest {

    @Test
    fun `month grid always contains six complete weeks`() {
        val grid = checkinMonthGrid(YearMonth.of(2026, 7))

        assertEquals(42, grid.size)
        assertEquals(LocalDate.of(2026, 7, 1), grid[2])
        assertEquals(LocalDate.of(2026, 7, 31), grid[32])
        assertNull(grid[41])
    }

    @Test
    fun `month range uses local midnight boundaries`() {
        val zoneId = ZoneId.of("Asia/Hong_Kong")
        val range = checkinMonthRange(YearMonth.of(2026, 7), zoneId)

        assertEquals(
            LocalDate.of(2026, 7, 1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            range.startInclusive
        )
        assertEquals(
            LocalDate.of(2026, 8, 1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            range.endExclusive
        )
    }

    @Test
    fun `records around local midnight are grouped into correct dates`() {
        val zoneId = ZoneId.of("Asia/Hong_Kong")
        val beforeMidnight = timestamp(2026, 7, 14, 23, 59, zoneId)
        val afterMidnight = timestamp(2026, 7, 15, 0, 1, zoneId)

        val grouped = groupCheckInsByLocalDate(
            listOf(
                CheckinData(timestamp = beforeMidnight),
                CheckinData(timestamp = afterMidnight)
            ),
            zoneId
        )

        assertEquals(1, grouped[LocalDate.of(2026, 7, 14)]?.size)
        assertEquals(1, grouped[LocalDate.of(2026, 7, 15)]?.size)
    }

    @Test
    fun `calendar state exposes records from selected date only`() {
        val july14 = LocalDate.of(2026, 7, 14)
        val july15 = LocalDate.of(2026, 7, 15)
        val july14Record = CheckinData(timestamp = 100L)
        val july15Records = listOf(
            CheckinData(timestamp = 200L),
            CheckinData(timestamp = 300L)
        )
        val state = CheckinCalendarUiState(
            visibleMonth = YearMonth.of(2026, 7),
            selectedDate = july15,
            recordsByDate = mapOf(
                july14 to listOf(july14Record),
                july15 to july15Records
            )
        )

        assertEquals(july15Records, state.selectedDateRecords)
    }

    private fun timestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        zoneId: ZoneId
    ): Long = LocalDateTime.of(year, month, day, hour, minute)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}
