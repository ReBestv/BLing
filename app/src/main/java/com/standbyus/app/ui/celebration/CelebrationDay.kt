package com.standbyus.app.ui.celebration

enum class CelebrationStyle {
    BIRTHDAY,
    HEARTS,
    FIREWORKS,
    CHRISTMAS,
    RED_GOLD
}

data class CelebrationDay(
    val id: String,
    val month: Int,
    val day: Int,
    val emoji: String,
    val message: String,
    val style: CelebrationStyle,
    val priority: Int
)
