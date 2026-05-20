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
        val heightScale = (availableHeightDp / 760f).coerceIn(0.82f, 1.04f)
        val cardGap = (18f * heightScale).coerceIn(12f, 20f)
        val buttonSize = when {
            availableHeightDp >= 840f -> 118f
            availableHeightDp >= 720f -> 108f
            else -> 100f
        }
        val buttonBottomPadding = if (availableHeightDp >= 700f) 14f else 8f
        val pkVerticalPadding = (26f * heightScale).coerceIn(20f, 28f)
        val statVerticalPadding = (18f * heightScale).coerceIn(14f, 20f)

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
