package com.standbyus.app.ui.history

data class HistoryLayoutMetrics(
    val horizontalPaddingDp: Float,
    val partnerBubbleMaxWidthDp: Float,
    val bubbleVerticalPaddingDp: Float,
    val dotSizeDp: Float
)

object HistoryLayout {
    fun metrics(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): HistoryLayoutMetrics {
        val compactWidth = availableWidthDp < 360f
        val compactHeight = availableHeightDp < 640f
        val compact = compactWidth || compactHeight

        return HistoryLayoutMetrics(
            horizontalPaddingDp = if (compactWidth) 12f else 16f,
            partnerBubbleMaxWidthDp = if (compactWidth) 228f else 260f,
            bubbleVerticalPaddingDp = if (compact) 14f else 16f,
            dotSizeDp = if (compact) 36f else 40f
        )
    }
}
