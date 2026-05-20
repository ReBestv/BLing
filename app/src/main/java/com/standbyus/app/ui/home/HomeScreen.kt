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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.ui.components.AppHeader
import com.standbyus.app.ui.components.StatusEmojiImage
import androidx.compose.material.icons.filled.Settings

// ── Design tokens (matching preview.html spec) ──────────────
private val BgColor       = Color(0xFFFFF8F5)
private val SurfaceColor  = Color(0xFFFFFFFF)
private val PrimaryColor  = Color(0xFFFFB4A2)
private val TextPrimary   = Color(0xFF5A4A42)
private val TextSecondary = Color(0xFF9E8E86)
private val BorderColor   = Color(0xFFF0EAE6)

// ── Main screen ─────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToPost: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val partnerStatus by viewModel.partnerStatus.collectAsState()
    val myStatus by viewModel.myStatus.collectAsState()
    val partnerDisplayName by viewModel.partnerDisplayName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // ── Sticky app bar ──
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

        // ── Content area (fills remaining space) ──
        if (partnerStatus != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                PartnerStatusCard(
                    status = partnerStatus!!,
                    userName = partnerDisplayName,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                MyStatusStrip(
                    status = myStatus,
                    onClick = onNavigateToPost,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
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

        // ── Publish button fixed at bottom ──
        QuickPostButton(
            onClick = onNavigateToPost,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Partner status card (gradient, emoji float, mood, note) ─

@Composable
private fun PartnerStatusCard(
    status: UserStatus,
    userName: String,
    modifier: Modifier = Modifier
) {
    val feeling = Feeling.fromDisplayName(status.feeling) ?: Feeling.HAPPY
    val cardGradient = cardGradientFor(feeling)

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
            }
            .padding(vertical = 56.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Large emoji with float animation
            StatusEmojiImage(
                value = status.feelingEmoji,
                feelingName = status.feeling,
                size = 132.dp,
                textSize = 80.sp,
                tintColor = feeling.color,
                modifier = Modifier.offset(y = floatOffset.dp)
            )

            // Mood text (28sp bold, white, with text-shadow)
            Text(
                text = status.feeling.ifEmpty { feeling.displayName },
                fontSize = 28.sp,
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

            // Activity text (16sp, white 90%)
            val doing = status.customDoing.ifEmpty { status.doing }
            if (doing.isNotEmpty()) {
                Text(
                    text = doing,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            // Optional note (italic, white 80%)
            if (status.note.isNotEmpty()) {
                Text(
                    text = status.note,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }

            // Relative time with clock icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${userName}刚刚更新了这个心情 · ${formatRelativeTime(status.updatedAt)}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "需要我哄哄${userName}吗？",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

// ── My status strip (clickable, emoji + doing + arrow) ──────

@Composable
private fun MyStatusStrip(
    status: UserStatus?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 0.dp,
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x1F5A4A42),
                spotColor = Color(0x1F5A4A42)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji
            StatusEmojiImage(
                value = status?.feelingEmoji,
                feelingName = status?.feeling,
                size = 48.dp,
                textSize = 40.sp
            )

            // Text
            val doing = status?.customDoing
                ?.ifEmpty { status.doing }
                ?.ifEmpty { status.feeling }
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

// ── Quick post button (custom, not Material3 Button) ────────

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

// ── Empty partner state ─────────────────────────────────────

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

// ── Emotion-based card gradient (feelings → warm colors) ───

private fun cardGradientFor(feeling: Feeling): List<Color> = when(feeling) {
    // 恋爱/开心/想你 → 粉色渐变
    Feeling.HAPPY, Feeling.LOVE, Feeling.KISS, Feeling.MISSING ->
        listOf(Color(0xFFFFD1DC), Color(0xFFFFB6C1))
    // 难过/生病/沮丧/焦虑/哭哭 → 淡蓝色
    Feeling.SAD, Feeling.SICK, Feeling.UPSET, Feeling.ANXIOUS, Feeling.CRYING ->
        listOf(Color(0xFFD4E6F1), Color(0xFFA9CCE3))
    // 生气 → 柔和红色
    Feeling.ANGRY ->
        listOf(Color(0xFFFFE0E0), Color(0xFFFFC8C8))
    // 疲惫/睡觉 → 薰衣草
    Feeling.TIRED, Feeling.SLEEPING ->
        listOf(Color(0xFFE8DAEF), Color(0xFFD2B4DE))
    // 其他（悠闲/奋斗/思考/追剧/无聊）→ 温暖蜜桃
    Feeling.RELAXED, Feeling.HUSTLING, Feeling.THINKING,
    Feeling.WATCHING, Feeling.BORED ->
        listOf(Color(0xFFFFE5CC), Color(0xFFFFD1A8))
}

// ── Helpers ─────────────────────────────────────────────────

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
