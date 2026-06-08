package com.standbyus.app.ui.celebration

enum class CelebrationOverlayContent {
    EMOJI,
    BIRTHDAY_IMAGE
}

fun CelebrationStyle.overlayContent(): CelebrationOverlayContent = when (this) {
    CelebrationStyle.BIRTHDAY -> CelebrationOverlayContent.BIRTHDAY_IMAGE
    CelebrationStyle.HEARTS,
    CelebrationStyle.FIREWORKS,
    CelebrationStyle.CHRISTMAS,
    CelebrationStyle.RED_GOLD -> CelebrationOverlayContent.EMOJI
}
