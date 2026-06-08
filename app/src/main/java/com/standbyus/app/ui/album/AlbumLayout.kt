package com.standbyus.app.ui.album

data class AlbumLayoutMetrics(
    val horizontalPaddingDp: Float,
    val filterVerticalPaddingDp: Float,
    val filterHorizontalPaddingDp: Float,
    val gridGapDp: Float,
    val sheetPaddingDp: Float,
    val sheetPreviewSizeDp: Float,
    val sheetSpacerDp: Float,
    val sheetButtonHeightDp: Float
)

object AlbumLayout {
    fun metrics(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): AlbumLayoutMetrics {
        val compact = availableWidthDp < 360f || availableHeightDp < 600f

        return AlbumLayoutMetrics(
            horizontalPaddingDp = if (compact) 16f else 24f,
            filterVerticalPaddingDp = if (compact) 6f else 8f,
            filterHorizontalPaddingDp = if (compact) 14f else 16f,
            gridGapDp = if (compact) 6f else 8f,
            sheetPaddingDp = if (compact) 16f else 20f,
            sheetPreviewSizeDp = if (compact) 132f else 160f,
            sheetSpacerDp = if (compact) 12f else 16f,
            sheetButtonHeightDp = if (compact) 46f else 48f
        )
    }
}
