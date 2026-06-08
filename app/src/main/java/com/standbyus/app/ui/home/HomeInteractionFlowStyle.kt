package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color

object HomeInteractionFlowStyle {
    const val topOverlapDp = 0f
    const val horizontalInsetDp = 10f
    const val topPaddingDp = 24f
    const val horizontalPaddingDp = 13f
    const val bottomPaddingDp = 14f
    const val itemGapDp = 12f
    const val actionGridGapDp = 10f
    const val cornerRadiusDp = 28f
    const val latestInteractionMinHeightDp = 42f
    const val myStatusMinHeightDp = 68f

    val expandedContentHeightDp: Float
        get() = topPaddingDp +
            bottomPaddingDp +
            latestInteractionMinHeightDp +
            myStatusMinHeightDp +
            (HomeInteractionActionButtonStyle.heightDp * 2f) +
            actionGridGapDp +
            (itemGapDp * 2f)

    val topColor = Color(0xC4FFFFFF)
    val bottomColor = Color(0x55FFFFFF)
}
