package com.standbyus.app.ui.poststatus

import org.junit.Assert.assertEquals
import org.junit.Test

class PostStatusLayoutTest {

    @Test
    fun `keeps mood grid spacious on regular phones`() {
        val metrics = PostStatusLayout.metrics(availableWidthDp = 393f, availableHeightDp = 700f)

        assertEquals(20f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(440f, metrics.moodGridHeightDp, 0.001f)
        assertEquals(64f, metrics.moodCircleSizeDp, 0.001f)
        assertEquals(58f, metrics.moodImageSizeDp, 0.001f)
        assertEquals(414f, metrics.stickerGridHeightDp, 0.001f)
        assertEquals(66f, metrics.stickerImageSizeDp, 0.001f)
        assertEquals(6f, metrics.stickerCardHorizontalPaddingDp, 0.001f)
        assertEquals(6f, metrics.stickerCardVerticalPaddingDp, 0.001f)
        assertEquals(10f, metrics.stickerGridHorizontalGapDp, 0.001f)
        assertEquals(10f, metrics.stickerGridVerticalGapDp, 0.001f)
        assertEquals(120f, metrics.noteHeightDp, 0.001f)
    }

    @Test
    fun `compresses controls on short phones`() {
        val metrics = PostStatusLayout.metrics(availableWidthDp = 393f, availableHeightDp = 520f)

        assertEquals(16f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(54f, metrics.moodCircleSizeDp, 0.001f)
        assertEquals(48f, metrics.moodImageSizeDp, 0.001f)
        assertEquals(376f, metrics.stickerGridHeightDp, 0.001f)
        assertEquals(58f, metrics.stickerImageSizeDp, 0.001f)
        assertEquals(4f, metrics.stickerCardHorizontalPaddingDp, 0.001f)
        assertEquals(6f, metrics.stickerCardVerticalPaddingDp, 0.001f)
        assertEquals(8f, metrics.stickerGridHorizontalGapDp, 0.001f)
        assertEquals(8f, metrics.stickerGridVerticalGapDp, 0.001f)
        assertEquals(96f, metrics.noteHeightDp, 0.001f)
    }

    @Test
    fun `sticker grid leaves room for all four rows`() {
        val metrics = PostStatusLayout.metrics(availableWidthDp = 393f, availableHeightDp = 520f)

        val rowHeight = 2 * metrics.stickerCardVerticalPaddingDp +
            metrics.stickerImageSizeDp + 4f + metrics.stickerLabelLineHeightSp
        val requiredHeight = 4 * rowHeight + 3 * metrics.stickerGridVerticalGapDp

        assert(metrics.stickerGridHeightDp >= requiredHeight) {
            "Sticker grid height ${metrics.stickerGridHeightDp}dp is smaller than required ${requiredHeight}dp"
        }
    }

    @Test
    fun `mood grid leaves room for the final row labels on compact screens`() {
        val metrics = PostStatusLayout.metrics(availableWidthDp = 393f, availableHeightDp = 520f)

        val rowCount = 4
        val itemVerticalPadding = 8f
        val selectedCircleVisualSize = metrics.moodCircleSizeDp * 1.12f
        val requiredHeight = rowCount * (
            itemVerticalPadding +
                selectedCircleVisualSize +
                metrics.labelTopGapDp +
                metrics.moodLabelLineHeightSp
            ) + (rowCount - 1) * metrics.gridVerticalGapDp

        assert(metrics.moodGridHeightDp >= requiredHeight) {
            "Mood grid height ${metrics.moodGridHeightDp}dp is smaller than required ${requiredHeight}dp"
        }
    }

    @Test
    fun `uses tighter gutters on narrow phones`() {
        val metrics = PostStatusLayout.metrics(availableWidthDp = 340f, availableHeightDp = 700f)

        assertEquals(16f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(8f, metrics.gridHorizontalGapDp, 0.001f)
    }
}
