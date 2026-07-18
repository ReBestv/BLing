package com.standbyus.app.ui.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.theme.Background
import com.standbyus.app.ui.theme.Border
import com.standbyus.app.ui.theme.Primary
import com.standbyus.app.ui.theme.PrimarySoft
import com.standbyus.app.ui.theme.Surface
import com.standbyus.app.ui.theme.TextHint
import com.standbyus.app.ui.theme.TextPrimary
import com.standbyus.app.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MonthTitleFormatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA)
private val SelectedDateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.CHINA)
private val WeekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

@Composable
fun CheckinCalendarScreen(
    onBack: () -> Unit,
    viewModel: CheckinCalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AppHeader(
            title = "打卡日历",
            onBack = onBack,
            showDivider = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            CheckinMonthCalendar(
                visibleMonth = state.visibleMonth,
                selectedDate = state.selectedDate,
                recordsByDate = state.recordsByDate,
                onPreviousMonth = viewModel::showPreviousMonth,
                onNextMonth = viewModel::showNextMonth,
                onToday = viewModel::returnToToday,
                onDateSelected = viewModel::selectDate
            )
            Spacer(modifier = Modifier.height(18.dp))
            SelectedDateHeader(
                date = state.selectedDate,
                count = state.selectedDateRecords.size
            )
            Spacer(modifier = Modifier.height(10.dp))

            when {
                state.isLoading -> CalendarLoadingState(Modifier.weight(1f))
                state.errorMessage != null -> CalendarErrorState(
                    message = state.errorMessage.orEmpty(),
                    onRetry = viewModel::retry,
                    modifier = Modifier.weight(1f)
                )
                state.selectedDateRecords.isEmpty() -> CalendarEmptyState(Modifier.weight(1f))
                else -> CheckinTimeList(
                    selectedDate = state.selectedDate,
                    records = state.selectedDateRecords,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CheckinMonthCalendar(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    recordsByDate: Map<LocalDate, List<CheckinData>>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val dates = remember(visibleMonth) { checkinMonthGrid(visibleMonth) }
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.72f), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上个月",
                    tint = TextSecondary
                )
            }
            Text(
                text = visibleMonth.format(MonthTitleFormatter),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下个月",
                    tint = TextSecondary
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onToday,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(text = "回到今天", color = Primary, fontSize = 12.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            WeekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextHint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        dates.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    if (date == null) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        CheckinCalendarDay(
                            date = date,
                            count = recordsByDate[date]?.size ?: 0,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckinCalendarDay(
    date: LocalDate,
    count: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityText = buildString {
        append("${date.monthValue}月${date.dayOfMonth}日")
        if (isToday) append("，今天")
        append("，${count}次打卡")
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .then(
                when {
                    isSelected -> Modifier.background(Primary)
                    isToday -> Modifier.border(1.5.dp, Primary, CircleShape)
                    else -> Modifier
                }
            )
            .semantics {
                contentDescription = accessibilityText
                selected = isSelected
            }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isSelected) Color.White else TextPrimary,
            fontSize = 14.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else PrimarySoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Primary,
                    fontSize = if (count > 9) 8.sp else 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SelectedDateHeader(date: LocalDate, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date.format(SelectedDateFormatter),
            modifier = Modifier.weight(1f),
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$count 次",
            color = Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(PrimarySoft)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CheckinTimeList(
    selectedDate: LocalDate,
    records: List<CheckinData>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        itemsIndexed(records, key = { _, item -> "${item.userId}_${item.timestamp}" }) { index, record ->
            CheckinTimeRow(record = record, number = records.size - index)
        }
    }
}

@Composable
private fun CheckinTimeRow(record: CheckinData, number: Int) {
    val time = remember(record.timestamp) {
        Instant.ofEpochMilli(record.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(TimeFormatter)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = "当天第 $number 次记录",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            if (record.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = record.note,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun CalendarLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
    }
}

@Composable
private fun CalendarEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = "这天还没有打卡记录",
            color = TextSecondary,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun CalendarErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, color = TextSecondary, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text(text = "重新加载", color = Primary)
        }
    }
}
