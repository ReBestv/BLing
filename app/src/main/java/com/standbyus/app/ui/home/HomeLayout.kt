package com.standbyus.app.ui.home

data class HomeLayoutMetrics(
    val horizontalPaddingDp: Float,
    val contentGapDp: Float,
    val topSpacerDp: Float,
    val bottomSpacerDp: Float,
    val partnerCardHeightDp: Float,
    val partnerCardVerticalPaddingDp: Float,
    val partnerEmojiSizeDp: Float,
    val partnerEmojiTextSizeSp: Float,
    val partnerContentGapDp: Float,
    val partnerEmojiLiftDp: Float
)

object HomeLayout {
    private const val NATURAL_PARTNER_CARD_HEIGHT_DP = 344f
    private const val MIN_PARTNER_CARD_HEIGHT_DP = 152.08f

    fun metrics(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): HomeLayoutMetrics {
        val horizontalPadding = if (availableWidthDp < 360f) 12f else 16f
        val verticalScale = (availableHeightDp / 620f).coerceIn(0.78f, 1.05f)
        val contentGap = (16f * verticalScale).coerceIn(10f, 16f)
        val topSpacer = (8f * verticalScale).coerceIn(4f, 8f)
        val bottomSpacer = (8f * verticalScale).coerceIn(4f, 8f)
        val availablePartnerHeight = MIN_PARTNER_CARD_HEIGHT_DP +
            (availableHeightDp - 420f) *
            ((NATURAL_PARTNER_CARD_HEIGHT_DP - MIN_PARTNER_CARD_HEIGHT_DP) / 200f)
        val partnerCardHeight = availablePartnerHeight
            .coerceIn(MIN_PARTNER_CARD_HEIGHT_DP, NATURAL_PARTNER_CARD_HEIGHT_DP)
        val partnerScale = (partnerCardHeight / NATURAL_PARTNER_CARD_HEIGHT_DP).coerceIn(0.62f, 1f)
        val partnerEmojiSize = (HomePartnerStatusCardStyle.emojiSizeDp * partnerScale)
            .coerceIn(124f, HomePartnerStatusCardStyle.emojiSizeDp)
        val partnerEmojiTextSize = (HomePartnerStatusCardStyle.emojiTextSizeSp * partnerScale)
            .coerceIn(84f, HomePartnerStatusCardStyle.emojiTextSizeSp)
        val partnerContentGap = (HomePartnerStatusCardStyle.contentGapDp * partnerScale)
            .coerceIn(2f, HomePartnerStatusCardStyle.contentGapDp)
        val partnerVerticalPadding = (HomePartnerStatusCardStyle.verticalPaddingDp * partnerScale)
            .coerceIn(18f, HomePartnerStatusCardStyle.verticalPaddingDp)
        val partnerEmojiLift = (HomePartnerStatusCardStyle.emojiLiftDp * partnerScale)
            .coerceIn(HomePartnerStatusCardStyle.emojiLiftDp, -4f)

        return HomeLayoutMetrics(
            horizontalPaddingDp = horizontalPadding,
            contentGapDp = contentGap,
            topSpacerDp = topSpacer,
            bottomSpacerDp = bottomSpacer,
            partnerCardHeightDp = partnerCardHeight,
            partnerCardVerticalPaddingDp = partnerVerticalPadding,
            partnerEmojiSizeDp = partnerEmojiSize,
            partnerEmojiTextSizeSp = partnerEmojiTextSize,
            partnerContentGapDp = partnerContentGap,
            partnerEmojiLiftDp = partnerEmojiLift
        )
    }
}
