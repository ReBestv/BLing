package com.standbyus.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.components.StatusEmojiImage
import com.standbyus.app.ui.components.UserAvatar
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// ——— Design tokens ———
private val TimelineDotBorder = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9E8E86)
private val FgColor = Color(0xFF5A4A42)
private val BgColor = Color(0xFFFFF8F5)
private val CardBg = Color(0xFFFFFFFF)
private val NeutralFeelingColor = Color(0xFFFFE5DC)

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val history by viewModel.statusHistory.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val myId by viewModel.myId.collectAsState()
    val partnerName by viewModel.partnerName.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        viewModel.startAutoRefresh()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
                viewModel.startAutoRefresh()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopAutoRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopAutoRefresh()
        }
    }

    val groupedHistory = remember(history) {
        history.groupBy { status ->
            val cal = Calendar.getInstance().apply { timeInMillis = status.updatedAt }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }.entries.sortedByDescending { it.key }
    }

    val myAvatar by viewModel.myAvatar.collectAsState()
    val myAvatarUrl by viewModel.myAvatarUrl.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BgColor)) {
        // ——— Custom App Bar ———
        AppHeader(
            title = "时光轴",
            onBack = onBack,
            rightIcon = {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "刷新",
                    tint = Color(0xFF9E8E86),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.refresh() }
                )
            }
        )

        if (history.isEmpty() && !loading) {
            // ——— Empty state ———
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "还没有状态记录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "分享你的第一个瞬间吧",
                        fontSize = 14.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val layoutMetrics = HistoryLayout.metrics(
                    availableWidthDp = maxWidth.value,
                    availableHeightDp = maxHeight.value
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (loading && history.isNotEmpty()) {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = layoutMetrics.horizontalPaddingDp.dp)
                            )
                        }
                    }

                    groupedHistory.forEach { (dateKey, statuses) ->
                        item { DateHeader(dateKey) }
                        items(statuses) { status ->
                            TimelineEntry(
                                status = status,
                                isMe = status.userId == myId,
                                partnerName = partnerName,
                                myAvatar = myAvatar,
                                myAvatarUrl = myAvatarUrl,
                                layoutMetrics = layoutMetrics
                            )
                        }
                    }
                }
            }
        }
    }
}

// ================================================================
// Date Header
// ================================================================
@Composable
private fun DateHeader(dateKey: String) {
    val parts = dateKey.split("-")
    val todayCal = Calendar.getInstance()

    val label = if (parts.size == 3) {
        val dateCal = Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }
        val daysDiff =
            ((todayCal.timeInMillis - dateCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        when {
            daysDiff == 0 -> "今天"
            daysDiff == 1 -> "昨天"
            daysDiff <= 7 -> "${daysDiff}天前"
            else -> SimpleDateFormat("M月d日", Locale.CHINESE).format(Date(dateCal.timeInMillis))
        }
    } else dateKey

    Text(
        text = label.uppercase(),
        fontSize = 14.sp,
        fontWeight = FontWeight(700),
        color = TextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 32.dp, top = 20.dp, bottom = 12.dp)
    )
}

// ================================================================
// Timeline Entry — dot + line + content card
// ================================================================
@Composable
private fun TimelineEntry(
    status: UserStatus,
    isMe: Boolean,
    partnerName: String,
    myAvatar: String,
    myAvatarUrl: String,
    layoutMetrics: HistoryLayoutMetrics
) {
    val feeling = Feeling.fromKey(status.feelingKey)
    val feelingColor = feeling?.color ?: NeutralFeelingColor
    val moodName = status.feelingLabel
        .ifEmpty { feeling?.displayName ?: status.stickerLabel.orEmpty() }
        .ifEmpty { "状态" }
    val activityText = status.customDoing
        .ifEmpty { status.doing }
        .ifEmpty { status.stickerLabel.orEmpty() }
        .ifEmpty { moodName }
    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(status.updatedAt))
    val bubbleModifier = when (val width = HistoryEntryLayout.bubbleWidth(isMe)) {
        is HistoryBubbleWidth.Fixed -> Modifier.width(
            minOf(width.width.value, layoutMetrics.partnerBubbleMaxWidthDp).dp
        )
        HistoryBubbleWidth.Wrap -> Modifier
            .widthIn(max = layoutMetrics.partnerBubbleMaxWidthDp.dp)
            .wrapContentWidth()
    }
    val dotSize = layoutMetrics.dotSizeDp.dp
    val entryAvatarUrl = if (isMe) myAvatarUrl.ifEmpty { status.avatarUrl } else status.avatarUrl
    val entryAvatarEmoji = if (isMe) myAvatar.ifEmpty { status.avatarEmoji } else status.avatarEmoji

    // Use parity of timestamp for hand-diary style rotation (+1 or -1)
    val rotationDeg = if ((status.updatedAt % 2).toInt() == 0) 1f else -1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = layoutMetrics.horizontalPaddingDp.dp, vertical = 6.dp)
            .graphicsLayer { rotationZ = rotationDeg },
        horizontalArrangement = HistoryEntryLayout.horizontalArrangement(isMe)
    ) {
        if (!isMe) {
            // ——— Other person's avatar dot (Left) ———
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 12.dp)
                    .size(dotSize)
                    .shadow(
                        6.dp,
                        CircleShape,
                        ambientColor = TimelineDotBorder.copy(alpha = 0.35f),
                        spotColor = TimelineDotBorder.copy(alpha = 0.35f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    avatarUrl = entryAvatarUrl,
                    avatarEmoji = entryAvatarEmoji,
                    size = dotSize,
                    textSize = (layoutMetrics.dotSizeDp * 0.52f).sp,
                    borderColor = TimelineDotBorder
                )
            }
        }

        // ——— Content card ———
        Card(
            modifier = bubbleModifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0x1F5A4A42),
                    spotColor = Color(0x1F5A4A42)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HistoryEntryLayout.cardMinHeight())
                    .padding(
                        horizontal = HistoryEntryLayout.cardHorizontalPadding(),
                        vertical = HistoryEntryLayout.cardVerticalPadding()
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = HistoryEntryLayout.cardContentArrangement(isMe)
            ) {
                if (!isMe) {
                    MoodBadge(
                        status = status,
                        feelingColor = feelingColor
                    )
                    Spacer(modifier = Modifier.width(HistoryEntryLayout.moodToTextGap()))
                }

                Column(
                    modifier = if (isMe) Modifier.wrapContentWidth() else Modifier.weight(1f),
                    horizontalAlignment = HistoryEntryLayout.bubbleContentAlignment(isMe)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Text(
                            text = HistoryDisplayName.resolveEntryDisplayName(
                                isMe = isMe,
                                partnerDisplayName = partnerName
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = FgColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedTime,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activityText,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    if (status.note.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = status.note,
                            fontSize = 13.sp,
                            color = TextSecondary.copy(alpha = 0.8f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                if (isMe) {
                    Spacer(modifier = Modifier.width(HistoryEntryLayout.moodToTextGap()))
                    MoodBadge(
                        status = status,
                        feelingColor = feelingColor
                    )
                }
            }
        }

        if (isMe) {
            // ——— My avatar dot (Right) ———
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, start = HistoryEntryLayout.avatarGap(isMe))
                    .size(dotSize)
                    .shadow(
                        6.dp,
                        CircleShape,
                        ambientColor = TimelineDotBorder.copy(alpha = 0.35f),
                        spotColor = TimelineDotBorder.copy(alpha = 0.35f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    avatarUrl = entryAvatarUrl,
                    avatarEmoji = entryAvatarEmoji,
                    size = dotSize,
                    textSize = (layoutMetrics.dotSizeDp * 0.52f).sp,
                    borderColor = TimelineDotBorder
                )
            }
        }
    }
}

@Composable
private fun MoodBadge(
    status: UserStatus,
    feelingColor: Color
) {
    Box(
        modifier = Modifier
            .size(HistoryEntryLayout.moodBadgeSize()),
        contentAlignment = Alignment.Center
    ) {
        StatusEmojiImage(
            value = status.feelingAsset,
            feelingKey = status.feelingKey,
            feelingLabel = status.feelingLabel,
            size = HistoryEntryLayout.moodImageSize(),
            textSize = 42.sp,
            tintColor = feelingColor,
            fallbackEmoji = status.feelingFallbackEmoji
        )
    }
}
