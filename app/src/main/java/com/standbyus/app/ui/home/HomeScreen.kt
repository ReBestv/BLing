package com.standbyus.app.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.statusBarsPadding
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.Interaction
import com.standbyus.app.data.model.InteractionType
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.components.StatusEmojiImage
import androidx.compose.material.icons.filled.Settings

// Design tokens (matching preview.html spec)
private val BgColor       = Color(0xFFFFF8F5)
private val SurfaceColor  = Color(0xFFFFFFFF)
private val PrimaryColor  = Color(0xFFFFB4A2)
private val TextPrimary   = Color(0xFF5A4A42)
private val TextSecondary = Color(0xFF9E8E86)
private val BorderColor   = Color(0xFFF0EAE6)
private val HomeBackgroundGradient = Brush.verticalGradient(
    colors = HomeBackgroundStyle.gradientColors
)

private fun UserStatus.primaryPartnerActionText(): String {
    return stickerLabel.orEmpty()
        .ifEmpty { customDoing.ifEmpty { doing } }
}

private fun UserStatus.partnerMoodText(): String = feelingLabel

// Main screen

@Composable
fun HomeScreen(
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val partnerStatus by viewModel.partnerStatus.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()
    val partnerDisplayName by viewModel.partnerDisplayName.collectAsState()
    val latestInteraction by viewModel.latestInteraction.collectAsState()
    val sendingInteractionType by viewModel.sendingInteractionType.collectAsState()
    val interactionError by viewModel.interactionError.collectAsState()

    LaunchedEffect(latestInteraction?.id) {
        viewModel.markLatestInteractionRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackgroundGradient)
    ) {
        // Sticky app bar
        AppHeader(
            title = "StandBy Us",
            rightIcon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = Color(0xFF9E8E86),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onNavigateToSettings() }
                )
            }
        )

        // Content area (fills remaining space)
        if (partnerStatus != null) {
            @Suppress("UnusedBoxWithConstraintsScope")
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val layoutMetrics = HomeLayout.metrics(
                    availableWidthDp = maxWidth.value,
                    availableHeightDp = maxHeight.value
                )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = layoutMetrics.horizontalPaddingDp.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(layoutMetrics.contentGapDp.dp)
            ) {
                Spacer(modifier = Modifier.height(layoutMetrics.topSpacerDp.dp))

                PartnerStatusCard(
                    status = partnerStatus!!,
                    userName = partnerDisplayName,
                    layoutMetrics = layoutMetrics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(layoutMetrics.partnerCardHeightDp.dp)
                )

                HomeInteractionFlow(
                    latestInteraction = latestInteraction,
                    partnerDisplayName = partnerDisplayName,
                    myStatus = myStatus,
                    sendingInteractionType = sendingInteractionType,
                    interactionError = interactionError,
                    onNavigateToPost = onNavigateToPost,
                    onActionClick = viewModel::sendInteraction,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(layoutMetrics.bottomSpacerDp.dp))
            }
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EmptyPartnerState(onGoToSettings = onNavigateToSettings)
            }
        }

        // Publish button fixed at bottom
        QuickPostButton(
            onClick = onNavigateToPost,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HomeInteractionFlow(
    latestInteraction: Interaction?,
    partnerDisplayName: String,
    myStatus: UserStatus?,
    sendingInteractionType: String?,
    interactionError: String,
    onNavigateToPost: () -> Unit,
    onActionClick: (InteractionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val flowShape = RoundedCornerShape(
        bottomStart = HomeInteractionFlowStyle.cornerRadiusDp.dp,
        bottomEnd = HomeInteractionFlowStyle.cornerRadiusDp.dp
    )

    Column(
        modifier = modifier
            .offset(y = HomeInteractionFlowStyle.topOverlapDp.dp)
            .padding(horizontal = HomeInteractionFlowStyle.horizontalInsetDp.dp)
            .clip(flowShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        HomeInteractionFlowStyle.topColor,
                        HomeInteractionFlowStyle.bottomColor
                    )
                )
            )
            .padding(
                start = HomeInteractionFlowStyle.horizontalPaddingDp.dp,
                top = HomeInteractionFlowStyle.topPaddingDp.dp,
                end = HomeInteractionFlowStyle.horizontalPaddingDp.dp,
                bottom = HomeInteractionFlowStyle.bottomPaddingDp.dp
            ),
        verticalArrangement = Arrangement.spacedBy(HomeInteractionFlowStyle.itemGapDp.dp)
    ) {
        HomeStatusInteractionOrder.sections(
            hasInteractionError = interactionError.isNotEmpty()
        ).forEach { section ->
            when (section) {
                HomeStatusInteractionSection.LATEST_INTERACTION -> LatestInteractionNotice(
                    interaction = latestInteraction,
                    partnerDisplayName = partnerDisplayName,
                    modifier = Modifier.fillMaxWidth()
                )

                HomeStatusInteractionSection.MY_STATUS -> MyStatusStrip(
                    status = myStatus,
                    onClick = onNavigateToPost,
                    modifier = Modifier.fillMaxWidth()
                )

                HomeStatusInteractionSection.INTERACTION_ACTIONS -> InteractionActionsGrid(
                    sendingType = sendingInteractionType,
                    onActionClick = onActionClick,
                    modifier = Modifier.fillMaxWidth()
                )

                HomeStatusInteractionSection.INTERACTION_ERROR -> Text(
                    text = interactionError,
                    fontSize = 12.sp,
                    color = Color(0xFFE45A45),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun LatestInteractionNotice(
    interaction: Interaction?,
    partnerDisplayName: String,
    modifier: Modifier = Modifier
) {
    if (interaction == null) return

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = HomeStatusBubbleStyle.latestInteractionContainerColor,
        shadowElevation = 0.dp,
        modifier = modifier
            .border(1.dp, HomeStatusBubbleStyle.borderColor, RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = HomeInteractionNoticeText.resolve(
                    interaction = interaction,
                    partnerDisplayName = partnerDisplayName
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InteractionActionsGrid(
    sendingType: String?,
    onActionClick: (InteractionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = HomeInteractionActions.quickActions.chunked(2)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEachIndexed { rowIndex, rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTypes.forEachIndexed { columnIndex, type ->
                    InteractionActionButton(
                        type = type,
                        actionIndex = rowIndex * 2 + columnIndex,
                        isSending = sendingType == type.key,
                        onClick = { onActionClick(type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTypes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InteractionActionButton(
    type: InteractionType,
    actionIndex: Int,
    isSending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(HomeInteractionActionButtonStyle.cornerRadiusDp.dp)

    Surface(
        onClick = onClick,
        enabled = !isSending,
        shape = buttonShape,
        color = HomeInteractionActionButtonStyle.containerColorForIndex(actionIndex),
        shadowElevation = 0.dp,
        modifier = modifier
            .height(HomeInteractionActionButtonStyle.heightDp.dp)
            .border(
                width = 1.dp,
                color = HomeInteractionActionButtonStyle.borderColor,
                shape = buttonShape
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = PrimaryColor
                )
            } else {
                Text(
                    text = type.sendText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

// Partner status card (gradient, emoji float, mood, note)

@Composable
private fun PartnerStatusCard(
    status: UserStatus,
    userName: String,
    layoutMetrics: HomeLayoutMetrics,
    modifier: Modifier = Modifier
) {
    val feeling = Feeling.fromKey(status.feelingKey)
    val displayFeeling = feeling ?: Feeling.RELAXED
    val cardGradient = HomePartnerStatusCardSurfaceStyle.gradientColorsFor(feeling)
    val primaryActionText = status.primaryPartnerActionText()
    val moodText = status.partnerMoodText()
    val detailText = status.note

    // Float animation for emoji: 3s ease-in-out, -8px
    val infiniteTransition = rememberInfiniteTransition(label = "emojiFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = PrimaryColor.copy(alpha = 0.15f),
                spotColor = PrimaryColor.copy(alpha = 0.15f)
            )
            .clip(cardShape)
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = cardGradient,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f)
                    )
                )
                drawOval(
                    color = HomePartnerStatusCardSurfaceStyle.topGlowColor,
                    topLeft = Offset(-size.width * 0.18f, -size.height * 0.22f),
                    size = Size(size.width * 0.82f, size.height * 0.46f)
                )
                drawOval(
                    color = HomePartnerStatusCardSurfaceStyle.warmGlowColor,
                    topLeft = Offset(size.width * 0.48f, size.height * 0.68f),
                    size = Size(size.width * 0.66f, size.height * 0.38f)
                )
            }
            .border(1.dp, HomePartnerStatusCardSurfaceStyle.borderColor, cardShape)
            .padding(vertical = layoutMetrics.partnerCardVerticalPaddingDp.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(layoutMetrics.partnerContentGapDp.dp)
        ) {
            // Large emoji with float animation
            StatusEmojiImage(
                value = status.feelingAsset,
                feelingKey = status.feelingKey,
                feelingLabel = status.feelingLabel,
                size = layoutMetrics.partnerEmojiSizeDp.dp,
                textSize = layoutMetrics.partnerEmojiTextSizeSp.sp,
                tintColor = displayFeeling.color,
                fallbackEmoji = status.feelingFallbackEmoji,
                modifier = Modifier.offset(y = (floatOffset + layoutMetrics.partnerEmojiLiftDp).dp)
            )

            if (primaryActionText.isNotEmpty()) {
                Text(
                    text = primaryActionText,
                    fontSize = HomePartnerStatusCardStyle.moodTextSizeSp.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = Offset(0f, 2f),
                            blurRadius = 8f
                        )
                    )
                )
            }

            if (moodText.isNotEmpty()) {
                Text(
                    text = moodText,
                    fontSize = HomePartnerStatusCardStyle.doingTextSizeSp.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            if (detailText.isNotEmpty()) {
                Text(
                    text = detailText,
                    fontSize = HomePartnerStatusCardStyle.noteTextSizeSp.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

        }
    }
}

// My status strip (clickable, emoji + doing + arrow)

@Composable
private fun MyStatusStrip(
    status: UserStatus?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = HomeStatusBubbleStyle.myStatusContainerColor,
        shadowElevation = 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji
            StatusEmojiImage(
                value = status?.feelingAsset,
                feelingKey = status?.feelingKey,
                feelingLabel = status?.feelingLabel,
                size = 48.dp,
                textSize = 40.sp,
                fallbackEmoji = status?.feelingFallbackEmoji
            )

            // Text
            val doing = status?.note
                ?.ifEmpty { status.primaryPartnerActionText() }
                ?.ifEmpty { status.partnerMoodText() }
                ?: "点击发布我的状态"
            Text(
                text = doing,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (status != null) TextPrimary else TextSecondary,
                modifier = Modifier.weight(1f)
            )

            // Arrow
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "发布状态",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Quick post button (custom, not Material3 Button)

@Composable
private fun QuickPostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(PrimaryColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "✨",
                fontSize = 16.sp
            )
            Text(
                text = "发布状态",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// Empty partner state

@Composable
private fun EmptyPartnerState(
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "💕", fontSize = 64.sp)

        Text(
            text = "还没有绑定伴侣",
            fontSize = 16.sp,
            color = TextSecondary
        )

        // Custom primary button
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(PrimaryColor)
                .clickable { onGoToSettings() }
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "去绑定",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// Helpers

private fun formatRelativeTime(updatedAt: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - updatedAt
    return when {
        diff < 0L           -> "刚刚"
        diff < 60_000L      -> "刚刚"
        diff < 3_600_000L   -> "${diff / 60_000L}分钟前"
        diff < 86_400_000L  -> "${diff / 3_600_000L}小时前"
        diff < 604_800_000L -> "${diff / 86_400_000L}天前"
        else                -> "很久以前"
    }
}
