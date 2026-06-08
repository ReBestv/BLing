package com.standbyus.app.ui.checkin

import kotlin.math.min

data class CheckinLayoutMetrics(
    val cardWidthDp: Float,
    val cardGapDp: Float,
    val buttonSizeDp: Float,
    val buttonBottomPaddingDp: Float,
    val pkVerticalPaddingDp: Float,
    val statVerticalPaddingDp: Float,
    val verticalScale: Float
)

object CheckinLayout {
    const val NATURAL_INFO_WIDTH_DP = 366f
    const val NATURAL_INFO_HEIGHT_DP = 620f

    fun infoScale(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): Float {
        val widthScale = availableWidthDp / NATURAL_INFO_WIDTH_DP
        val heightScale = availableHeightDp / NATURAL_INFO_HEIGHT_DP
        return min(1f, min(widthScale, heightScale))
    }

    fun metrics(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): CheckinLayoutMetrics {
        val cardWidth = (availableWidthDp * 0.9f).coerceIn(288f, 420f)
        val heightScale = (availableHeightDp / 760f).coerceIn(0.78f, 1f)
        val cardGap = (12f * heightScale).coerceIn(8f, 12f)
        val buttonSize = when {
            availableHeightDp >= 840f -> 104f
            availableHeightDp >= 720f -> 96f
            else -> 88f
        }
        val buttonBottomPadding = if (availableHeightDp >= 700f) 8f else 4f
        val pkVerticalPadding = (18f * heightScale).coerceIn(14f, 20f)
        val statVerticalPadding = (12f * heightScale).coerceIn(10f, 14f)

        return CheckinLayoutMetrics(
            cardWidthDp = cardWidth,
            cardGapDp = cardGap,
            buttonSizeDp = buttonSize,
            buttonBottomPaddingDp = buttonBottomPadding,
            pkVerticalPaddingDp = pkVerticalPadding,
            statVerticalPaddingDp = statVerticalPadding,
            verticalScale = heightScale
        )
    }
}
