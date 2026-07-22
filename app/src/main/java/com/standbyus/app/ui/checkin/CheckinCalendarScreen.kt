package com.standbyus.app.ui.checkin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.theme.Background
import com.standbyus.app.ui.theme.Border
import com.standbyus.app.ui.theme.Partner
import com.standbyus.app.ui.theme.PartnerSoft
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val accentColor = if (state.recordOwner == CheckinCalendarOwner.ME) Primary else Partner
    val accentSoftColor = if (state.recordOwner == CheckinCalendarOwner.ME) PrimarySoft else PartnerSoft

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPairingState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
            Spacer(modifier = Modifier.height(8.dp))
            CheckinOwnerSwitch(
                selectedOwner = state.recordOwner,
                partnerDisplayName = state.partnerDisplayName,
                isPaired = state.isPaired,
                onOwnerSelected = viewModel::selectRecordOwner
            )
            OwnerContextRow(
                recordsTitle = state.selectedOwnerRecordsTitle,
                syncLabel = "已同步",
                accentColor = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            CheckinMonthCalendar(
                visibleMonth = state.visibleMonth,
                selectedDate = state.selectedDate,
                recordsByDate = state.recordsByDate,
                accentColor = accentColor,
                accentSoftColor = accentSoftColor,
                onPreviousMonth = viewModel::showPreviousMonth,
                onNextMonth = viewModel::showNextMonth,
                onToday = viewModel::returnToToday,
                onDateSelected = viewModel::selectDate
            )
            Spacer(modifier = Modifier.height(14.dp))
            SelectedDateHeader(
                date = state.selectedDate,
                count = state.selectedDateRecords.size,
                accentColor = accentColor,
                accentSoftColor = accentSoftColor
            )
            Spacer(modifier = Modifier.height(10.dp))

            when {
                state.isLoading -> CalendarLoadingState(
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                state.errorMessage != null -> CalendarErrorState(
                    message = state.errorMessage.orEmpty(),
                    onRetry = viewModel::retry,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                state.selectedDateRecords.isEmpty() -> CalendarEmptyState(
                    message = if (state.recordOwner == CheckinCalendarOwner.ME) {
                        "这天还没有打卡记录"
                    } else {
                        "${state.partnerDisplayName}这天还没有打卡记录"
                    },
                    modifier = Modifier.weight(1f)
                )
                else -> CheckinTimeList(
                    selectedDate = state.selectedDate,
                    records = state.selectedDateRecords,
                    recordOwner = state.recordOwner,
                    ownerCheckinLabel = state.selectedOwnerCheckinLabel,
                    accentColor = accentColor,
                    accentSoftColor = accentSoftColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CheckinOwnerSwitch(
    selectedOwner: CheckinCalendarOwner,
    partnerDisplayName: String,
    isPaired: Boolean,
    onOwnerSelected: (CheckinCalendarOwner) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OwnerSwitchOption(
            label = "我的记录",
            avatarText = "我",
            selected = selectedOwner == CheckinCalendarOwner.ME,
            enabled = true,
            accentColor = Primary,
            accentSoftColor = PrimarySoft,
            onClick = { onOwnerSelected(CheckinCalendarOwner.ME) },
            modifier = Modifier.weight(1f)
        )
        OwnerSwitchOption(
            label = "${partnerDisplayName}的记录",
            avatarText = partnerDisplayName.firstOrNull()?.toString().orEmpty().ifEmpty { "对" },
            selected = selectedOwner == CheckinCalendarOwner.PARTNER,
            enabled = isPaired,
            accentColor = Partner,
            accentSoftColor = PartnerSoft,
            onClick = { onOwnerSelected(CheckinCalendarOwner.PARTNER) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OwnerSwitchOption(
    label: String,
    avatarText: String,
    selected: Boolean,
    enabled: Boolean,
    accentColor: Color,
    accentSoftColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) accentSoftColor else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "ownerSwitchBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) TextPrimary else TextSecondary,
        animationSpec = tween(durationMillis = 180),
        label = "ownerSwitchText"
    )

    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .alpha(if (enabled) 1f else 0.42f)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                contentDescription = if (enabled) label else "$label，尚未绑定"
            }
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarText,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OwnerContextRow(
    recordsTitle: String,
    syncLabel: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "正在查看", color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = recordsTitle,
            modifier = Modifier.weight(1f),
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = syncLabel,
            color = TextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CheckinMonthCalendar(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    recordsByDate: Map<LocalDate, List<CheckinData>>,
    accentColor: Color,
    accentSoftColor: Color,
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
                Text(text = "回到今天", color = accentColor, fontSize = 12.sp)
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
                            accentColor = accentColor,
                            accentSoftColor = accentSoftColor,
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
    accentColor: Color,
    accentSoftColor: Color,
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
                    isSelected -> Modifier.background(accentColor)
                    isToday -> Modifier.border(1.5.dp, accentColor, CircleShape)
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
                    .background(if (isSelected) Color.White else accentSoftColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = accentColor,
                    fontSize = if (count > 9) 8.sp else 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SelectedDateHeader(
    date: LocalDate,
    count: Int,
    accentColor: Color,
    accentSoftColor: Color
) {
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
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(accentSoftColor)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun CheckinTimeList(
    selectedDate: LocalDate,
    records: List<CheckinData>,
    recordOwner: CheckinCalendarOwner,
    ownerCheckinLabel: String,
    accentColor: Color,
    accentSoftColor: Color,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate, recordOwner) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        itemsIndexed(records, key = { _, item -> "${item.userId}_${item.timestamp}" }) { index, record ->
            CheckinTimeRow(
                record = record,
                number = records.size - index,
                ownerCheckinLabel = ownerCheckinLabel,
                accentColor = accentColor,
                accentSoftColor = accentSoftColor
            )
        }
    }
}

@Composable
private fun CheckinTimeRow(
    record: CheckinData,
    number: Int,
    ownerCheckinLabel: String,
    accentColor: Color,
    accentSoftColor: Color
) {
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
                text = ownerCheckinLabel,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = record.note.ifBlank { "当天第 $number 次记录" },
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accentSoftColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarLoadingState(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = accentColor, strokeWidth = 3.dp)
    }
}

@Composable
private fun CalendarEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun CalendarErrorState(
    message: String,
    onRetry: () -> Unit,
    accentColor: Color,
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
            Text(text = "重新加载", color = accentColor)
        }
    }
}
