package com.standbyus.app.ui.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbyus.app.R
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

private data class GiftParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val wobble: Float,
    val phase: Float,
    val size: Float,
    val color: Color
)

private data class RibbonParticle(
    val x: Float,
    val y: Float,
    val width: Float,
    val color: Color,
    val phase: Float,
    val mirror: Boolean
)

private data class BurstRibbonParticle(
    val targetX: Float,
    val targetY: Float,
    val width: Float,
    val height: Float,
    val stroke: Float,
    val color: Color,
    val delay: Float,
    val startRotation: Float,
    val endRotation: Float,
    val scale: Float,
    val mirror: Boolean
)

private data class BurstParticle(
    val targetX: Float,
    val targetY: Float,
    val size: Float,
    val color: Color,
    val delay: Float,
    val scale: Float,
    val rotation: Float
)

@Composable
fun CelebrationOverlay(
    celebration: CelebrationDay,
    onDismiss: () -> Unit
) {
    val visualSpec = remember(celebration.style) { celebration.style.visualSpec() }
    val giftSpec = remember(celebration.style) { celebration.style.giftEffectSpec() }
    var isGiftOpened by remember(celebration.id) { mutableStateOf(false) }
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

    LaunchedEffect(giftSpec.enabled) {
        if (!giftSpec.enabled) {
            delay(4000)
            onDismiss()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "celebrationProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(visualSpec.background))
            .clickable {
                if (giftSpec.enabled && !isGiftOpened) {
                    isGiftOpened = true
                } else {
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (giftSpec.enabled) {
            BirthdayGiftEffect(
                spec = giftSpec,
                progress = progress,
                isGiftOpened = isGiftOpened
            )
        } else {
            LegacyCelebrationParticles(
                visualSpec = visualSpec,
                particles = particles,
                progress = progress
            )
            LegacyCelebrationContent(
                celebration = celebration,
                scale = scale.value,
                showMessage = giftSpec.showMessage
            )
        }
    }
}

@Composable
private fun LegacyCelebrationParticles(
    visualSpec: CelebrationVisualSpec,
    particles: List<Particle>,
    progress: Float
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

            drawCircle(
                color = particle.color,
                radius = particle.size,
                center = Offset(drawX, animatedY * size.height)
            )
        }
    }
}

@Composable
private fun LegacyCelebrationContent(
    celebration: CelebrationDay,
    scale: Float,
    showMessage: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        when (celebration.style.overlayContent()) {
            CelebrationOverlayContent.BIRTHDAY_IMAGE -> Image(
                painter = painterResource(id = R.drawable.birthday_splash),
                contentDescription = "生日快乐",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            )
            CelebrationOverlayContent.EMOJI -> Text(
                text = celebration.emoji,
                fontSize = 72.sp
            )
        }
        if (showMessage) {
            Text(
                text = celebration.message,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun BirthdayGiftEffect(
    spec: BirthdayGiftEffectSpec,
    progress: Float,
    isGiftOpened: Boolean
) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(isGiftOpened) {
        if (isGiftOpened) {
            reveal.snapTo(0f)
            reveal.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
            )
        } else {
            reveal.snapTo(0f)
        }
    }
    val timeline = reveal.value

    val starParticles = remember(spec.fallingStarCount) {
        (0 until spec.fallingStarCount).map { index ->
            GiftParticle(
                x = ((index * 0.083f) + Random.nextFloat() * 0.08f) % 1f,
                y = -0.18f - Random.nextFloat(),
                speed = 0.28f + Random.nextFloat() * 0.26f,
                wobble = 8f + Random.nextFloat() * 22f,
                phase = Random.nextFloat() * 6.28f,
                size = 11f + Random.nextFloat() * 9f,
                color = spec.starColors[index % spec.starColors.size]
            )
        }
    }
    val hearts = remember(spec.floatingHeartCount) {
        val colors = listOf(
            Color(0xFFFF7FB1),
            Color(0xFFFFBFD6),
            Color(0xFFFF6F91),
            Color(0xFFFF9DC7),
            Color(0xFFFFD1E1)
        )
        (0 until spec.floatingHeartCount).map { index ->
            GiftParticle(
                x = ((index * 0.11f) + Random.nextFloat() * 0.1f) % 1f,
                y = -0.24f - Random.nextFloat(),
                speed = 0.2f + Random.nextFloat() * 0.2f,
                wobble = 14f + Random.nextFloat() * 28f,
                phase = Random.nextFloat() * 6.28f,
                size = 9f + Random.nextFloat() * 7f,
                color = colors[index % colors.size]
            )
        }
    }
    val ribbons = remember(spec.ribbonCount) {
        val colors = listOf(
            Color(0xFFFFF5A8),
            Color(0xFFFF9ECC),
            Color(0xFF82E0C5),
            Color(0xFF78D2FF),
            Color(0xFFFF8B63)
        )
        (0 until spec.ribbonCount).map { index ->
            RibbonParticle(
                x = 0.12f + (index % 5) * 0.18f + Random.nextFloat() * 0.06f,
                y = 0.56f - (index / 2) * 0.045f + Random.nextFloat() * 0.04f,
                width = 48f + Random.nextFloat() * 34f,
                color = colors[index % colors.size],
                phase = Random.nextFloat() * 6.28f,
                mirror = index % 2 == 1
            )
        }
    }
    val burstRibbons = remember(spec.burstRibbonCount) {
        val colors = listOf(
            Color(0xFFFFF5A8),
            Color(0xFFFF9ECC),
            Color(0xFF82E0C5),
            Color(0xFF78D2FF),
            Color(0xFFFF8B63),
            Color(0xFFD5B0FF)
        )
        val targets = listOf(
            -0.42f to -0.42f,
            0.4f to -0.4f,
            -0.24f to -0.5f,
            0.22f to -0.49f,
            -0.5f to -0.3f,
            0.48f to -0.32f,
            -0.08f to -0.56f,
            0.06f to -0.58f
        )
        (0 until spec.burstRibbonCount).map { index ->
            val target = targets[index % targets.size]
            BurstRibbonParticle(
                targetX = target.first,
                targetY = target.second,
                width = 126f + (index % 3) * 18f,
                height = 28f + (index % 4) * 3f,
                stroke = 7f + (index % 3),
                color = colors[index % colors.size],
                delay = index * 0.018f,
                startRotation = -28f + index * 9f,
                endRotation = if (index % 2 == 0) -52f - index * 2f else 42f + index * 4f,
                scale = 1.04f + (index % 4) * 0.06f,
                mirror = index % 2 == 1
            )
        }
    }
    val burstSparks = remember(spec.burstSparkCount) {
        val colors = listOf(
            Color(0xFFFFF7A8),
            Color(0xFFFF9FD0),
            Color(0xFF82DFFF),
            Color(0xFFB8FFCE),
            Color(0xFFFFC36F),
            Color(0xFFD5B0FF)
        )
        val targets = listOf(
            -0.32f to -0.46f,
            0.34f to -0.44f,
            -0.12f to -0.54f,
            0.16f to -0.52f,
            -0.5f to -0.34f,
            0.52f to -0.34f
        )
        (0 until spec.burstSparkCount).map { index ->
            val target = targets[index % targets.size]
            BurstParticle(
                targetX = target.first,
                targetY = target.second,
                size = 16f + (index % 3) * 3f,
                color = colors[index % colors.size],
                delay = 0.03f + index * 0.024f,
                scale = 1.3f + (index % 4) * 0.12f,
                rotation = 210f + index * 18f
            )
        }
    }
    val burstPetals = remember(spec.burstPetalCount) {
        val colors = listOf(
            Color(0xFFFFBFD6),
            Color(0xFFFFF5A8),
            Color(0xFF82E0C5),
            Color(0xFF78D2FF),
            Color(0xFFFF8B63)
        )
        val targets = listOf(
            -0.4f to -0.34f,
            0.4f to -0.36f,
            -0.22f to -0.46f,
            0.22f to -0.48f,
            -0.04f to -0.54f
        )
        (0 until spec.burstPetalCount).map { index ->
            val target = targets[index % targets.size]
            BurstParticle(
                targetX = target.first,
                targetY = target.second,
                size = 12f + (index % 2) * 2f,
                color = colors[index % colors.size],
                delay = 0.04f + index * 0.03f,
                scale = 1.08f + (index % 3) * 0.12f,
                rotation = if (index % 2 == 0) 160f else -180f
            )
        }
    }

    val rainReveal = if (isGiftOpened) delayedProgress(timeline, 0.08f, 0.18f) else 0f
    val imageReveal = delayedProgress(timeline, 0.32f, 0.42f)
    val crownReveal = delayedProgress(timeline, 0.45f, 0.32f)
    val burstReveal = delayedProgress(timeline, 0f, 0.55f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cycle = progress / 1000f
        drawBurstRibbons(burstRibbons, burstReveal)
        drawBurstSparks(burstSparks, burstReveal)
        drawBurstPetals(burstPetals, burstReveal)
        drawGiftRibbons(ribbons, cycle, burstReveal)
        drawFallingStars(starParticles, cycle, rainReveal)
        drawFloatingHearts(hearts, cycle, rainReveal)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val imageSize = birthdayImageSize()
        val imageTop = birthdayImageTop()
        val giftBottom = birthdayGiftBottom()
        val crownTop = birthdayCrownTop()

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = imageTop)
                .size(imageSize)
                .graphicsLayer {
                    val eased = overshootProgress(imageReveal)
                    alpha = imageReveal
                    scaleX = 0.18f + 0.9f * eased
                    scaleY = 0.18f + 0.9f * eased
                    rotationZ = -8f + 11f * imageReveal - 3f * delayedProgress(imageReveal, 0.6f, 0.4f)
                    translationY = (1f - eased) * maxHeight.toPx() * 0.42f - sin(imageReveal * PI.toFloat()) * 22f
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF7DA), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.birthday_splash),
                    contentDescription = "生日快乐",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (spec.showCrown) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = crownTop)
                    .size(width = 172.dp, height = 84.dp)
                    .graphicsLayer {
                        val eased = overshootProgress(crownReveal)
                        alpha = crownReveal
                        scaleX = 0.7f + 0.4f * eased
                        scaleY = 0.7f + 0.4f * eased
                        rotationZ = -10f + 15f * crownReveal - 9f * delayedProgress(crownReveal, 0.55f, 0.45f)
                        translationY = (1f - eased) * 36f - sin(crownReveal * PI.toFloat()) * 8f
                    }
            ) {
                drawCrown()
            }
        }

        if (spec.showGiftBox) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = giftBottom)
                    .size(width = 190.dp, height = 150.dp)
                    .graphicsLayer {
                        val thump = giftThumpScale(timeline)
                        scaleX = thump.first
                        scaleY = thump.second
                        translationY = giftThumpTranslation(timeline)
                    }
            ) {
                drawGiftBox(timeline)
            }
        }
    }
}

private fun birthdayImageSize(): Dp = 310.dp

private fun birthdayImageTop(): Dp = 132.dp

private fun birthdayCrownTop(): Dp = 30.dp

private fun birthdayGiftBottom(): Dp = 72.dp

private fun delayedProgress(
    value: Float,
    delay: Float,
    duration: Float
): Float {
    if (duration <= 0f) return 1f
    return ((value - delay) / duration).coerceIn(0f, 1f)
}

private fun easeOutCubic(value: Float): Float {
    val inverse = 1f - value.coerceIn(0f, 1f)
    return 1f - inverse * inverse * inverse
}

private fun overshootProgress(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f) * (t - 1f) * (t - 1f) + c1 * (t - 1f) * (t - 1f)
}

private fun giftThumpScale(timeline: Float): Pair<Float, Float> = when {
    timeline < 0.1f -> 1f to 1f
    timeline < 0.18f -> {
        val p = delayedProgress(timeline, 0.1f, 0.08f)
        (1f + 0.18f * p) to (1f - 0.18f * p)
    }
    timeline < 0.32f -> {
        val p = delayedProgress(timeline, 0.18f, 0.14f)
        (1.18f - 0.26f * p) to (0.82f + 0.33f * p)
    }
    timeline < 0.5f -> {
        val p = delayedProgress(timeline, 0.32f, 0.18f)
        (0.92f + 0.12f * p) to (1.15f - 0.15f * p)
    }
    else -> 1f to 1f
}

private fun giftThumpTranslation(timeline: Float): Float = when {
    timeline < 0.1f -> 0f
    timeline < 0.18f -> delayedProgress(timeline, 0.1f, 0.08f) * 8f
    timeline < 0.32f -> 8f - delayedProgress(timeline, 0.18f, 0.14f) * 28f
    timeline < 0.5f -> -20f + delayedProgress(timeline, 0.32f, 0.18f) * 20f
    else -> 0f
}

private fun DrawScope.drawFallingStars(
    stars: List<GiftParticle>,
    cycle: Float,
    reveal: Float
) {
    if (reveal <= 0.01f) return
    val particleScale = density * 1.18f
    stars.forEach { star ->
        val yCycle = (star.y + cycle * star.speed * 2.1f) % 1.25f
        val x = star.x * size.width + sin(cycle * 12f + star.phase) * star.wobble * particleScale
        val y = yCycle * size.height
        val alpha = (if (yCycle < 0.02f || yCycle > 1.08f) 0.15f else 0.9f) * reveal
        drawStar(
            center = Offset(x, y),
            radius = star.size * particleScale,
            color = star.color.copy(alpha = alpha),
            rotation = cycle * 180f + star.phase * 20f
        )
    }
}

private fun DrawScope.drawBurstRibbons(
    ribbons: List<BurstRibbonParticle>,
    reveal: Float
) {
    val particleScale = density
    ribbons.forEach { ribbon ->
        val p = delayedProgress(reveal, ribbon.delay, 0.74f)
        if (p <= 0.01f) return@forEach
        val eased = easeOutCubic(p)
        val alpha = when {
            p < 0.22f -> p / 0.22f
            p < 0.58f -> 1f
            else -> (1f - delayedProgress(p, 0.58f, 0.42f)) * 0.94f + 0.06f
        }
        val origin = Offset(size.width * 0.5f, size.height - 146f)
        val x = origin.x + ribbon.targetX * size.width * eased
        val y = origin.y + ribbon.targetY * size.height * eased + (1f - eased) * 18f
        val rotation = ribbon.startRotation + (ribbon.endRotation - ribbon.startRotation) * eased
        drawCurledRibbon(
            center = Offset(x, y),
            width = ribbon.width * particleScale * (0.24f + ribbon.scale * eased),
            height = ribbon.height * particleScale * (0.35f + eased),
            color = ribbon.color.copy(alpha = alpha),
            strokeWidth = ribbon.stroke * particleScale,
            rotation = rotation,
            mirror = ribbon.mirror
        )
    }
}

private fun DrawScope.drawBurstSparks(
    sparks: List<BurstParticle>,
    reveal: Float
) {
    val particleScale = density
    sparks.forEach { spark ->
        val p = delayedProgress(reveal, spark.delay, 0.68f)
        if (p <= 0.01f) return@forEach
        val eased = easeOutCubic(p)
        val alpha = when {
            p < 0.22f -> p / 0.22f
            p < 0.72f -> 1f
            else -> 1f - delayedProgress(p, 0.72f, 0.28f)
        }
        val origin = Offset(size.width * 0.5f, size.height - 162f)
        val center = Offset(
            origin.x + spark.targetX * size.width * eased,
            origin.y + spark.targetY * size.height * eased + (1f - eased) * 12f
        )
        drawStar(
            center = center,
            radius = spark.size * particleScale * (0.3f + spark.scale * eased),
            color = spark.color.copy(alpha = alpha),
            rotation = spark.rotation * eased
        )
    }
}

private fun DrawScope.drawBurstPetals(
    petals: List<BurstParticle>,
    reveal: Float
) {
    val particleScale = density
    petals.forEach { petal ->
        val p = delayedProgress(reveal, petal.delay, 0.72f)
        if (p <= 0.01f) return@forEach
        val eased = easeOutCubic(p)
        val alpha = when {
            p < 0.25f -> p / 0.25f
            else -> 1f - delayedProgress(p, 0.25f, 0.75f)
        }
        val origin = Offset(size.width * 0.5f, size.height - 154f)
        val center = Offset(
            origin.x + petal.targetX * size.width * eased,
            origin.y + petal.targetY * size.height * eased + (1f - eased) * 12f
        )
        drawPetal(
            center = center,
            size = petal.size * particleScale * (0.3f + petal.scale * eased),
            color = petal.color.copy(alpha = alpha),
            rotation = petal.rotation * eased
        )
    }
}

private fun DrawScope.drawFloatingHearts(
    hearts: List<GiftParticle>,
    cycle: Float,
    reveal: Float
) {
    if (reveal <= 0.01f) return
    val particleScale = density * 1.28f
    hearts.forEach { heart ->
        val yCycle = (heart.y + cycle * heart.speed * 1.8f) % 1.28f
        val x = heart.x * size.width + sin(cycle * 10f + heart.phase) * heart.wobble * particleScale
        val y = yCycle * size.height
        val alpha = (if (yCycle > 1.08f) 0.1f else 0.68f) * reveal
        drawHeart(
            center = Offset(x, y),
            size = heart.size * particleScale,
            color = heart.color.copy(alpha = alpha)
        )
    }
}

private fun DrawScope.drawGiftRibbons(
    ribbons: List<RibbonParticle>,
    cycle: Float,
    reveal: Float
) {
    if (reveal <= 0.01f) return
    val particleScale = density
    ribbons.forEach { ribbon ->
        val x = ribbon.x * size.width
        val y = ribbon.y * size.height - reveal * 120f * particleScale + sin(cycle * 12f + ribbon.phase) * 8f * particleScale
        val width = ribbon.width * particleScale
        val start = Offset(x - width / 2f, y)
        val end = Offset(x + width / 2f, y + if (ribbon.mirror) -14f * particleScale else 14f * particleScale)
        val control = Offset(x, y - 34f * particleScale - sin(cycle * 8f + ribbon.phase) * 12f * particleScale)
        val path = Path().apply {
            moveTo(start.x, start.y)
            quadraticTo(control.x, control.y, end.x, end.y)
        }
        drawPath(
            path = path,
            color = ribbon.color.copy(alpha = 0.86f * reveal),
            style = Stroke(width = 3.6f * particleScale, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawGiftBox(timeline: Float) {
    val centerX = size.width / 2f
    val baseWidth = size.width * 0.68f
    val baseHeight = size.height * 0.42f
    val baseLeft = centerX - baseWidth / 2f
    val baseTop = size.height * 0.48f
    val lidWidth = baseWidth * 1.12f
    val lidHeight = size.height * 0.24f
    val lidLeft = centerX - lidWidth / 2f
    val lidOpen = easeOutCubic(delayedProgress(timeline, 0.1f, 0.32f))
    val lidTop = size.height * 0.25f - lidOpen * 76f
    val lidCenter = Offset(centerX - lidOpen * 46f, lidTop + lidHeight / 2f)
    val lidRotation = -24f * lidOpen - sin(lidOpen * PI.toFloat()) * 10f

    drawOval(
        color = Color(0x5527192D),
        topLeft = Offset(baseLeft + 12f, baseTop + baseHeight - 2f),
        size = Size(baseWidth - 24f, 24f)
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFF5D78), Color(0xFFFF8A53), Color(0xFFFFB35F)),
            start = Offset(baseLeft, baseTop),
            end = Offset(baseLeft + baseWidth, baseTop + baseHeight)
        ),
        topLeft = Offset(baseLeft, baseTop),
        size = Size(baseWidth, baseHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color(0xFFFFE07A),
        topLeft = Offset(centerX - baseWidth * 0.08f, baseTop),
        size = Size(baseWidth * 0.16f, baseHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = Color(0x55FFFFFF),
        topLeft = Offset(baseLeft, baseTop),
        size = Size(baseWidth, 14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )
    drawRotatedGiftLid(
        center = lidCenter,
        width = lidWidth,
        height = lidHeight,
        rotation = lidRotation
    )
    drawGiftBow(
        center = Offset(centerX + lidOpen * 12f, size.height * 0.25f + 2f - lidOpen * 76f),
        reveal = lidOpen
    )
}

private fun DrawScope.drawRotatedGiftLid(
    center: Offset,
    width: Float,
    height: Float,
    rotation: Float
) {
    rotate(
        degrees = rotation,
        pivot = Offset(center.x - width * 0.36f, center.y + height * 0.5f)
    ) {
        val topLeft = Offset(center.x - width / 2f, center.y - height / 2f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFFFF6F89), Color(0xFFFF9B5F), Color(0xFFFFC06D)),
                start = topLeft,
                end = Offset(topLeft.x + width, topLeft.y + height)
            ),
            topLeft = topLeft,
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
        drawRoundRect(
            color = Color(0xFFFFE07A),
            topLeft = Offset(center.x - width * 0.08f, topLeft.y),
            size = Size(width * 0.16f, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0x55FFFFFF),
            topLeft = topLeft,
            size = Size(width, 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
    }
}

private fun DrawScope.drawGiftBow(
    center: Offset,
    reveal: Float
) {
    val bowColor = Color(0xFFFFE36F).copy(alpha = 0.96f - 0.2f * reveal)
    val bowLift = reveal * 82f
    val bowShift = reveal * 12f
    val bowScale = 1f + sin(reveal * PI.toFloat()) * 0.22f - reveal * 0.14f
    drawOval(
        color = bowColor,
        topLeft = Offset(center.x - 45f + bowShift, center.y - 30f - bowLift),
        size = Size(46f * bowScale, 32f * bowScale),
        style = Stroke(width = 8f)
    )
    drawOval(
        color = bowColor,
        topLeft = Offset(center.x - 1f + bowShift, center.y - 30f - bowLift),
        size = Size(46f * bowScale, 32f * bowScale),
        style = Stroke(width = 8f)
    )
    drawCircle(color = bowColor, radius = 12f * bowScale, center = Offset(center.x + bowShift, center.y - 11f - bowLift))
}

private fun DrawScope.drawCrown() {
    val crownPath = Path().apply {
        moveTo(size.width * 0.08f, size.height * 0.9f)
        lineTo(size.width * 0.08f, size.height * 0.42f)
        lineTo(size.width * 0.24f, size.height * 0.68f)
        lineTo(size.width * 0.36f, size.height * 0.1f)
        lineTo(size.width * 0.5f, size.height * 0.62f)
        lineTo(size.width * 0.64f, size.height * 0.1f)
        lineTo(size.width * 0.76f, size.height * 0.68f)
        lineTo(size.width * 0.92f, size.height * 0.42f)
        lineTo(size.width * 0.92f, size.height * 0.9f)
        close()
    }
    drawPath(
        path = crownPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFB72F), Color(0xFFFFE97D), Color(0xFFFF9F2E)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFF3A3), Color(0xFFFFBB3E))),
        topLeft = Offset(size.width * 0.12f, size.height * 0.78f),
        size = Size(size.width * 0.76f, size.height * 0.18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(999f, 999f)
    )
    val gems = listOf(
        Offset(size.width * 0.34f, size.height * 0.78f) to Color(0xFF7DD7FF),
        Offset(size.width * 0.5f, size.height * 0.76f) to Color(0xFFFF6F9F),
        Offset(size.width * 0.66f, size.height * 0.78f) to Color(0xFFB6FF9C)
    )
    gems.forEach { (offset, color) ->
        drawCircle(color = color, radius = 5.4f, center = offset)
    }
}

private fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color,
    rotation: Float
) {
    val path = Path()
    val points = 10
    for (i in 0 until points) {
        val angle = ((i * 36f + rotation - 90f) * PI / 180f).toFloat()
        val r = if (i % 2 == 0) radius else radius * 0.42f
        val point = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path = path, color = color)
}

private fun DrawScope.drawCurledRibbon(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    strokeWidth: Float,
    rotation: Float,
    mirror: Boolean
) {
    rotate(degrees = rotation, pivot = center) {
        scale(scaleX = if (mirror) -1f else 1f, scaleY = 1f, pivot = center) {
            val start = Offset(center.x - width * 0.5f, center.y + height * 0.28f)
            val control1 = Offset(center.x - width * 0.24f, center.y - height * 0.8f)
            val control2 = Offset(center.x + width * 0.24f, center.y + height * 0.86f)
            val end = Offset(center.x + width * 0.5f, center.y - height * 0.18f)
            val path = Path().apply {
                moveTo(start.x, start.y)
                cubicTo(control1.x, control1.y, control2.x, control2.y, end.x, end.y)
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

private fun DrawScope.drawPetal(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)
            cubicTo(
                center.x + size * 0.8f,
                center.y - size * 0.72f,
                center.x + size * 0.65f,
                center.y + size * 0.6f,
                center.x,
                center.y + size
            )
            cubicTo(
                center.x - size * 0.58f,
                center.y + size * 0.42f,
                center.x - size * 0.72f,
                center.y - size * 0.58f,
                center.x,
                center.y - size
            )
            close()
        }
        drawPath(path = path, color = color)
    }
}

private fun DrawScope.drawHeart(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y + size * 0.52f)
        cubicTo(
            center.x - size * 1.2f,
            center.y - size * 0.12f,
            center.x - size * 0.62f,
            center.y - size * 1.0f,
            center.x,
            center.y - size * 0.42f
        )
        cubicTo(
            center.x + size * 0.62f,
            center.y - size * 1.0f,
            center.x + size * 1.2f,
            center.y - size * 0.12f,
            center.x,
            center.y + size * 0.52f
        )
        close()
    }
    drawPath(path = path, color = color)
}

private fun CelebrationStyle.visualSpec(): CelebrationVisualSpec = when (this) {
    CelebrationStyle.BIRTHDAY -> CelebrationVisualSpec(
        background = listOf(Color(0xFFFF8FAB), Color(0xFFFFA764), Color(0xFF8EC5FC)),
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
