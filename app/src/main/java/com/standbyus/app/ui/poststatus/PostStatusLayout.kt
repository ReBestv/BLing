package com.standbyus.app.ui.poststatus

data class PostStatusLayoutMetrics(
    val horizontalPaddingDp: Float,
    val topSpacerDp: Float,
    val moodGridHeightDp: Float,
    val gridHorizontalGapDp: Float,
    val gridVerticalGapDp: Float,
    val moodCircleSizeDp: Float,
    val moodImageSizeDp: Float,
    val moodRowCircleSizeDp: Float,
    val moodRowImageSizeDp: Float,
    val moodRowItemWidthDp: Float,
    val stickerGridHeightDp: Float,
    val stickerImageSizeDp: Float,
    val stickerCardHorizontalPaddingDp: Float,
    val stickerCardVerticalPaddingDp: Float,
    val stickerGridHorizontalGapDp: Float,
    val stickerGridVerticalGapDp: Float,
    val stickerLabelLineHeightSp: Float,
    val moodEmojiTextSizeSp: Float,
    val moodLabelLineHeightSp: Float,
    val labelTopGapDp: Float,
    val sectionGapDp: Float,
    val noteHeightDp: Float,
    val bottomButtonHeightDp: Float,
    val bottomSpacerDp: Float
)

object PostStatusLayout {
    private const val MoodGridRows = 4
    private const val MoodItemVerticalPaddingDp = 8f
    private const val MoodLabelLineHeightDp = 20f
    private const val SelectedMoodScale = 1.12f

    fun metrics(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): PostStatusLayoutMetrics {
        val compactWidth = availableWidthDp < 360f
        val compactHeight = availableHeightDp < 600f
        val verticalScale = (availableHeightDp / 700f).coerceIn(0.78f, 1f)
        val gridVerticalGapDp = if (compactHeight) 10f else 16f
        val moodCircleSizeDp = if (compactHeight) 54f else 64f
        val labelTopGapDp = if (compactHeight) 4f else 6f
        val stickerImageSizeDp = if (compactHeight) 58f else 66f
        val stickerLabelLineHeightSp = 14f
        val stickerGridHorizontalGapDp = if (compactWidth || compactHeight) 8f else 10f
        val stickerGridVerticalGapDp = if (compactHeight) 8f else 10f
        val stickerCardHorizontalPaddingDp = if (compactWidth || compactHeight) 4f else 6f
        val stickerCardVerticalPaddingDp = 6f
        val stickerImageLabelGapDp = 4f
        val stickerRowHeightDp = 2 * stickerCardVerticalPaddingDp + stickerImageSizeDp +
            stickerImageLabelGapDp + stickerLabelLineHeightSp
        val stickerGridHeightDp = 4 * stickerRowHeightDp + 3 * stickerGridVerticalGapDp
        val requiredMoodGridHeightDp = MoodGridRows * (
            MoodItemVerticalPaddingDp +
                moodCircleSizeDp * SelectedMoodScale +
                labelTopGapDp +
                MoodLabelLineHeightDp
            ) + (MoodGridRows - 1) * gridVerticalGapDp
        val moodGridHeightDp = if (compactHeight) {
            requiredMoodGridHeightDp
        } else {
            440f
        }

        return PostStatusLayoutMetrics(
            horizontalPaddingDp = if (compactWidth || compactHeight) 16f else 20f,
            topSpacerDp = (16f * verticalScale).coerceIn(8f, 16f),
            moodGridHeightDp = moodGridHeightDp,
            gridHorizontalGapDp = if (compactWidth) 8f else 12f,
            gridVerticalGapDp = gridVerticalGapDp,
            moodCircleSizeDp = moodCircleSizeDp,
            moodImageSizeDp = if (compactHeight) 48f else 58f,
            moodRowCircleSizeDp = if (compactHeight) 46f else 50f,
            moodRowImageSizeDp = if (compactHeight) 38f else 42f,
            moodRowItemWidthDp = if (compactHeight || compactWidth) 58f else 64f,
            stickerGridHeightDp = stickerGridHeightDp,
            stickerImageSizeDp = stickerImageSizeDp,
            stickerCardHorizontalPaddingDp = stickerCardHorizontalPaddingDp,
            stickerCardVerticalPaddingDp = stickerCardVerticalPaddingDp,
            stickerGridHorizontalGapDp = stickerGridHorizontalGapDp,
            stickerGridVerticalGapDp = stickerGridVerticalGapDp,
            stickerLabelLineHeightSp = stickerLabelLineHeightSp,
            moodEmojiTextSizeSp = if (compactHeight) 28f else 32f,
            moodLabelLineHeightSp = MoodLabelLineHeightDp,
            labelTopGapDp = labelTopGapDp,
            sectionGapDp = (18f * verticalScale).coerceIn(10f, 24f),
            noteHeightDp = if (compactHeight) 96f else 120f,
            bottomButtonHeightDp = if (compactHeight) 52f else 56f,
            bottomSpacerDp = if (compactHeight) 16f else 24f
        )
    }
}
