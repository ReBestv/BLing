package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color

object HomeInteractionActionButtonStyle {
    const val heightDp = 42f
    const val cornerRadiusDp = 18f

    val borderColor = Color(0x33FFB4A2)

    private val containerColors = listOf(
        Color(0xFFFFF0EC),
        Color(0xFFF7F0FF),
        Color(0xFFFFF7E2),
        Color(0xFFF0F6FF)
    )

    fun containerColorForIndex(index: Int): Color =
        containerColors[index.floorMod(containerColors.size)]
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
