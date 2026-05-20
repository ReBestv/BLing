package com.standbyus.app.ui.checkin

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.ui.components.AppHeader

// ── Design Tokens ──
private val BgColor = Color(0xFFFFF8F5)
private val SurfaceColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFFFFB4A2)
private val GradientEnd = Color(0xFFFF9E8E)
private val TextPrimary = Color(0xFF5A4A42)
private val TextSecondary = Color(0xFF9E8E86)
private val BorderColor = Color(0xFFF0EAE6)
private val WinAccent = Color(0xFFE8B84B)
private val DangerAccent = Color(0xFFE07070)

// ── Main Screen ──

@Composable
fun CheckinScreen(
    onBack: () -> Unit,
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── App Bar ──
            AppHeader(
                title = "拉了么",
                showDivider = false // Usually matching home/other pages?
            )

            // ── Scrollable content (takes remaining space) ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // ── PK Section (top) ──
                if (state.isPaired && state.pkStats != null) {
                    PKCard(stats = state.pkStats!!)
                } else {
                    EmptyPKCard()
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Today Stats Card ──
                ThreeColCard(
                    col1 = { StatCell("💩", "${state.todayCount} 次", "已记录", PrimaryColor.copy(alpha = 0.15f)) },
                    col2 = { StatCell("⏰", state.lastInterval, "间隔", Color(0xFFE3F2FD)) },
                    col3 = { StatCell(state.riskLevel.emoji, state.riskLevel.label, "便秘风险", Color(0xFFFFF8E1)) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── This Week Stats Card ──
                ThreeColCard(
                    col1 = { StatCell("📊", "${state.weeklyTotal} 次", "总共", Color(0xFFE3F2FD)) },
                    col2 = { StatCell("📈", "${state.weeklyAverage}/天", "平均", Color(0xFFE8F5E9)) },
                    col3 = { StatCell("🔥", "${state.streak} 天", "连续打卡", Color(0xFFFFF3E0)) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            // ── Big Checkin Button (fixed, not scrollable) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BigCheckinButton(
                    onClick = { viewModel.checkIn() },
                    isCheckingIn = state.isCheckingIn
                )
            }

            // Space for bottom nav
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Three Column Card ──

@Composable
private fun ThreeColCard(
    col1: @Composable () -> Unit,
    col2: @Composable () -> Unit,
    col3: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceColor)
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { col1() }
        VerticalDivider()
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { col2() }
        VerticalDivider()
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { col3() }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(BorderColor)
    )
}

@Composable
private fun StatCell(
    emoji: String,
    value: String,
    label: String,
    circleBg: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Emoji in circle background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

// ── PK Card ──

@Composable
private fun PKCard(stats: PKStats) {
    val isMeWinning = stats.winner == "me"
    val isTie = stats.winner == null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceColor)
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "🏆 本月拉屎大王对决",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // VS Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // My side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isMeWinning) {
                    Text(text = "👑", fontSize = 24.sp)
                }
                Text(
                    text = "我",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMeWinning) WinAccent else TextPrimary
                )
                Text(
                    text = "${stats.myCount} 次",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMeWinning) WinAccent else TextPrimary
                )
            }

            Text(
                text = if (isTie) "🤝" else "VS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            // Partner side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isMeWinning && !isTie) {
                    Text(text = "👑", fontSize = 24.sp)
                }
                Text(
                    text = "TA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!isMeWinning && !isTie) WinAccent else TextPrimary
                )
                Text(
                    text = "${stats.partnerCount} 次",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isMeWinning && !isTie) WinAccent else TextPrimary
                )
            }
        }

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF0EAE6))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(stats.myPercentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isMeWinning) WinAccent else PrimaryColor)
            )
        }

        // Stats detail
        Text(
            text = when {
                isTie -> "🤝 不分胜负！势均力敌！"
                isMeWinning -> "🎉 领先 ${stats.leadAmount} 次！"
                else -> "💪 落后 ${stats.leadAmount} 次，继续努力！"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        if (!isTie) {
            Text(
                text = if (isMeWinning) {
                    "距离月底还有 ${stats.daysUntilMonthEnd} 天，TA 每天需多拉 ${stats.partnerCatchUpRate} 次才能追"
                } else {
                    "距离月底还有 ${stats.daysUntilMonthEnd} 天，每天多拉 ${stats.myCatchUpRate} 次就能反超！"
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyPKCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceColor)
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🏆 本月拉屎大王对决",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "绑定伴侣后即可与 TA PK",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

// ── Big Checkin Button ──

@Composable
private fun BigCheckinButton(
    onClick: () -> Unit,
    isCheckingIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    val rippleProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, 1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val rippleProgress3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, 2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple3"
    )

    Box(
        modifier = modifier
            .size(100.dp)
            .drawBehind {
                val rippleAlpha = (1f - rippleProgress1) * 0.4f
                val rippleScale = 1f + rippleProgress1 * 0.5f
                drawCircle(
                    color = PrimaryColor.copy(alpha = rippleAlpha),
                    radius = size.minDimension / 2f * rippleScale,
                    style = Stroke(width = 2.dp.toPx())
                )
                val rippleAlpha2 = (1f - rippleProgress2) * 0.4f
                val rippleScale2 = 1f + rippleProgress2 * 0.5f
                drawCircle(
                    color = PrimaryColor.copy(alpha = rippleAlpha2),
                    radius = size.minDimension / 2f * rippleScale2,
                    style = Stroke(width = 2.dp.toPx())
                )
                val rippleAlpha3 = (1f - rippleProgress3) * 0.4f
                val rippleScale3 = 1f + rippleProgress3 * 0.5f
                drawCircle(
                    color = PrimaryColor.copy(alpha = rippleAlpha3),
                    radius = size.minDimension / 2f * rippleScale3,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0x3F5A4A42),
                spotColor = Color(0x3F5A4A42)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(PrimaryColor, GradientEnd)
                )
            )
            .clickable(enabled = !isCheckingIn) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "💩", fontSize = 40.sp)
    }
}
