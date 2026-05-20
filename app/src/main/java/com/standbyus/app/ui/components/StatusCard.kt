package com.standbyus.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.ui.theme.StandByUsLightColors
import com.standbyus.app.ui.theme.StandByUsMotion
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusCard(
    status: UserStatus?,
    userName: String,
    modifier: Modifier = Modifier
) {
    val feeling = status?.let { Feeling.fromKey(it.feelingKey) } ?: Feeling.HAPPY

    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(StandByUsMotion.DUR_BREATH, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    val cardShape = RoundedCornerShape(24.dp)
    val bgGradient = Brush.verticalGradient(
        colors = listOf(feeling.gradientStart, feeling.gradientEnd)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, cardShape, ambientColor = StandByUsLightColors.accentGlow.copy(alpha = glowAlpha))
            .clip(cardShape)
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            StatusEmojiImage(
                value = status?.feelingAsset,
                feelingKey = status?.feelingKey,
                feelingLabel = status?.feelingLabel,
                size = 132.dp,
                textSize = 96.sp,
                tintColor = feeling.color,
                fallbackEmoji = status?.feelingFallbackEmoji
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = status?.let { it.customDoing.ifEmpty { it.doing }.ifEmpty { it.feelingLabel } } ?: "—",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = StandByUsLightColors.fg
            )
            if (!status?.note.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status?.note ?: "",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = StandByUsLightColors.fgSecondary
                )
            }
        }
    }
}
