package com.standbyus.app.ui.celebration

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
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

private data class CelebrationVisualSpec(
    val background: List<Color>,
    val particles: List<Color>,
    val particleCount: Int,
    val motion: ParticleMotion
)

private enum class ParticleMotion {
    FALL,
    FLOAT,
    BURST,
    TWINKLE
}

@Composable
fun CelebrationOverlay(
    celebration: CelebrationDay,
    onDismiss: () -> Unit
) {
    val visualSpec = remember(celebration.style) { celebration.style.visualSpec() }

    val particles = remember(celebration.id) {
        (1..visualSpec.particleCount).map {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f,
                speed = 0.2f + Random.nextFloat() * 0.5f,
                wobblePhase = Random.nextFloat() * 6.28f,
                wobbleSpeed = 0.5f + Random.nextFloat() * 1.5f,
                size = 8f + Random.nextFloat() * 12f,
                color = visualSpec.particles[Random.nextInt(visualSpec.particles.size)]
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
            .background(
                Brush.verticalGradient(visualSpec.background)
            )
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val cycle = progress / 1000f
                val animatedY = when (visualSpec.motion) {
                    ParticleMotion.FLOAT -> 1.05f - ((particle.y + cycle * particle.speed) % 1.2f)
                    ParticleMotion.BURST -> 0.5f + sin(cycle * 2f * PI.toFloat() + particle.wobblePhase) * particle.speed
                    else -> (particle.y + cycle * particle.speed) % 1.1f
                }
                val wobbleX = when (visualSpec.motion) {
                    ParticleMotion.BURST -> cos(cycle * 2f * PI.toFloat() + particle.wobblePhase) * size.width * 0.28f
                    ParticleMotion.TWINKLE -> sin(progress / 80f * particle.wobbleSpeed + particle.wobblePhase) * 10f
                    else -> sin(progress / 100f * particle.wobbleSpeed + particle.wobblePhase) * 20f
                }

                val drawX = if (visualSpec.motion == ParticleMotion.BURST) {
                    size.width * 0.5f + wobbleX
                } else {
                    particle.x * size.width + wobbleX
                }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
        ) {
            Text(
                text = celebration.emoji,
                fontSize = 72.sp
            )
            Text(
                text = celebration.message,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

private fun CelebrationStyle.visualSpec(): CelebrationVisualSpec = when (this) {
    CelebrationStyle.BIRTHDAY -> CelebrationVisualSpec(
        background = listOf(Color(0xFFFF8FAB), Color(0xFFFFC857), Color(0xFF8EC5FC)),
        particles = listOf(Color(0xFFFFF3B0), Color(0xFFFFA3C7), Color(0xFFFF7A59), Color(0xFF8EC5FC)),
        particleCount = 34,
        motion = ParticleMotion.FALL
    )
    CelebrationStyle.HEARTS -> CelebrationVisualSpec(
        background = listOf(Color(0xFFFF7AA2), Color(0xFFD56BFF), Color(0xFFFFC2D1)),
        particles = listOf(Color(0xFFFFF0F6), Color(0xFFFF8FAB), Color(0xFFFF4D8D), Color(0xFFE9D5FF)),
        particleCount = 28,
        motion = ParticleMotion.FLOAT
    )
    CelebrationStyle.FIREWORKS -> CelebrationVisualSpec(
        background = listOf(Color(0xFF182848), Color(0xFF4B6CB7), Color(0xFFFFD166)),
        particles = listOf(Color(0xFFFFF7AE), Color(0xFFFFFFFF), Color(0xFFFF7AA2), Color(0xFF8EC5FC)),
        particleCount = 30,
        motion = ParticleMotion.BURST
    )
    CelebrationStyle.CHRISTMAS -> CelebrationVisualSpec(
        background = listOf(Color(0xFF0F766E), Color(0xFF14532D), Color(0xFFE11D48)),
        particles = listOf(Color(0xFFFFFFFF), Color(0xFFE0F2FE), Color(0xFFFFD166), Color(0xFFDCFCE7)),
        particleCount = 32,
        motion = ParticleMotion.FALL
    )
    CelebrationStyle.RED_GOLD -> CelebrationVisualSpec(
        background = listOf(Color(0xFFB91C1C), Color(0xFFDC2626), Color(0xFFFACC15)),
        particles = listOf(Color(0xFFFFF7AE), Color(0xFFFACC15), Color(0xFFFFEDD5), Color(0xFFFFFFFF)),
        particleCount = 30,
        motion = ParticleMotion.TWINKLE
    )
}
