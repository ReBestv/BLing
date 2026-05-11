package com.standbyus.app.ui.celebration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var wobblePhase: Float,
    var wobbleSpeed: Float,
    var size: Float,
    var color: Color
)

@Composable
fun CelebrationOverlay(
    celebration: CelebrationDay,
    onDismiss: () -> Unit
) {
    val colors = remember {
        listOf(
            Color(0xFFFF9F43),
            Color(0xFFFFD93D),
            Color(0xFF74B9FF),
            Color(0xFFFD79A8),
            Color(0xFF55EFC4),
            Color(0xFFA29BFE),
            Color(0xFF00CEC9),
        )
    }

    val particles = remember {
        (1..20).map {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f,
                speed = 0.2f + Random.nextFloat() * 0.5f,
                wobblePhase = Random.nextFloat() * 6.28f,
                wobbleSpeed = 0.5f + Random.nextFloat() * 1.5f,
                size = 8f + Random.nextFloat() * 12f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(alpha = 0.45f))
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val t = progress / 1000f
            for (p in particles) {
                p.y = ((p.y + p.speed * t / 20f) % 1.2f) - 0.2f
                p.x += sin(p.wobblePhase + t * p.wobbleSpeed) * 0.005f
                p.x = p.x.coerceIn(0f, 1f)

                drawCircle(
                    color = p.color,
                    radius = p.size,
                    center = Offset(p.x * w, p.y * h)
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            ) {
                Text(
                    text = celebration.emoji,
                    fontSize = 72.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = celebration.message,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
