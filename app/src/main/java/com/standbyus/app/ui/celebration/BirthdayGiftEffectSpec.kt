package com.standbyus.app.ui.celebration

import androidx.compose.ui.graphics.Color

data class BirthdayGiftEffectSpec(
    val enabled: Boolean,
    val showCrown: Boolean,
    val showGiftBox: Boolean,
    val showMessage: Boolean,
    val fallingStarCount: Int,
    val floatingHeartCount: Int,
    val ribbonCount: Int,
    val burstRibbonCount: Int,
    val burstSparkCount: Int,
    val burstPetalCount: Int,
    val starColors: List<Color>
)

fun CelebrationStyle.giftEffectSpec(): BirthdayGiftEffectSpec = when (this) {
    CelebrationStyle.BIRTHDAY -> BirthdayGiftEffectSpec(
        enabled = true,
        showCrown = true,
        showGiftBox = true,
        showMessage = false,
        fallingStarCount = 34,
        floatingHeartCount = 16,
        ribbonCount = 16,
        burstRibbonCount = 8,
        burstSparkCount = 6,
        burstPetalCount = 5,
        starColors = listOf(
            Color(0xFFFFF7A8),
            Color(0xFFFF9FD0),
            Color(0xFF82DFFF),
            Color(0xFFB8FFCE),
            Color(0xFFD5B0FF)
        )
    )
    CelebrationStyle.HEARTS,
    CelebrationStyle.FIREWORKS,
    CelebrationStyle.CHRISTMAS,
    CelebrationStyle.RED_GOLD -> BirthdayGiftEffectSpec(
        enabled = false,
        showCrown = false,
        showGiftBox = false,
        showMessage = true,
        fallingStarCount = 0,
        floatingHeartCount = 0,
        ribbonCount = 0,
        burstRibbonCount = 0,
        burstSparkCount = 0,
        burstPetalCount = 0,
        starColors = emptyList()
    )
}
