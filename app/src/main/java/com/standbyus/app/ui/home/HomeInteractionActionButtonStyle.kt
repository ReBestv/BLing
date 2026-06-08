package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color

object HomeInteractionActionButtonStyle {
    const val heightDp = 52f
    const val cornerRadiusDp = 22f

    val borderColor = Color(0x2EFF8E78)

    private val containerColors = listOf(
        Color(0xCCFFFFFF),
        Color(0xE6FFF2ED),
        Color(0xE6FFF7E2),
        Color(0xE6F1F8F3)
    )

    fun containerColorForIndex(index: Int): Color =
        containerColors[index.floorMod(containerColors.size)]
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
