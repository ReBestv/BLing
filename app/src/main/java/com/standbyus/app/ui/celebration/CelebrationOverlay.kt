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
import com.standbyus.app.ui.theme.StandByUsLightColors
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
            StandByUsLightColors.accent,      // coral
            StandByUsLightColors.accentHover, // deep coral
            StandByUsLightColors.accentSoft,  // soft coral
            StandByUsLightColors.success,     // green
            StandByUsLightColors.warn,        // gold
            Color(0xFFFFB5A7),                // warm pink
            Color(0xFFA8E6CF),                // soft mint
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

    // ...keep rest of the file (animation logic, Canvas, etc.)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val animatedY = (particle.y + progress * particle.speed / 1000f) % 1.1f
                val wobbleX = sin(progress / 100f * particle.wobbleSpeed + particle.wobblePhase) * 20f

                val drawX = particle.x * size.width + wobbleX
                val drawY = animatedY * size.height

                drawCircle(
                    color = particle.color,
                    radius = particle.size,
                    center = Offset(drawX, drawY)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        ) {
            Text(
                text = celebration.emoji,
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = celebration.message,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
