package com.standbyus.app.ui.checkin

import com.standbyus.app.data.model.CheckinData
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

internal data class CheckinMonthRange(
    val startInclusive: Long,
    val endExclusive: Long
)

internal fun checkinMonthRange(
    month: YearMonth,
    zoneId: ZoneId = ZoneId.systemDefault()
): CheckinMonthRange {
    val start = month.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val end = month.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    return CheckinMonthRange(startInclusive = start, endExclusive = end)
}

internal fun checkinMonthGrid(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val leadingEmptyDays = firstDay.dayOfWeek.value - 1
    val dates = buildList<LocalDate?> {
        repeat(leadingEmptyDays) { add(null) }
        for (day in 1..month.lengthOfMonth()) {
            add(month.atDay(day))
        }
    }
    return dates + List(42 - dates.size) { null }
}

internal fun groupCheckInsByLocalDate(
    records: List<CheckinData>,
    zoneId: ZoneId = ZoneId.systemDefault()
): Map<LocalDate, List<CheckinData>> = records
    .sortedByDescending { it.timestamp }
    .groupBy { record ->
        Instant.ofEpochMilli(record.timestamp).atZone(zoneId).toLocalDate()
    }
