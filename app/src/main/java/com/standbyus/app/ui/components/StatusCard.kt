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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.UserStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusCard(
    status: UserStatus?,
    userName: String,
    modifier: Modifier = Modifier
) {
    val feeling = status?.let { Feeling.fromDisplayName(it.feeling) } ?: Feeling.RELAXED

    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    val cardShape = RoundedCornerShape(20.dp)
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            feeling.gradientStart,
            feeling.gradientEnd
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, cardShape, ambientColor = feeling.color.copy(alpha = glowAlpha))
            .clip(cardShape)
            .background(bgGradient)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = feeling.emoji,
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status?.let { it.customDoing.ifEmpty { it.doing } } ?: "—",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3436)
            )
            if (!status?.note.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status?.note ?: "",
                    fontSize = 16.sp,
                    color = Color(0xFF2D3436).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = status?.let {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "更新于 ${sdf.format(Date(it.updatedAt))}"
                } ?: "暂无状态",
                fontSize = 12.sp,
                color = Color(0xFF2D3436).copy(alpha = 0.5f)
            )
        }
    }
}
