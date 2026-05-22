package com.standbyus.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeLayoutTest {

    @Test
    fun `keeps partner card expressive on regular phones`() {
        val metrics = HomeLayout.metrics(availableWidthDp = 393f, availableHeightDp = 620f)

        assertEquals(16f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(320f, metrics.partnerCardHeightDp, 0.001f)
        assertEquals(156f, metrics.partnerEmojiSizeDp, 0.001f)
        assertEquals(4f, metrics.partnerContentGapDp, 0.001f)
    }

    @Test
    fun `compresses partner card and emoji on short phones`() {
        val metrics = HomeLayout.metrics(availableWidthDp = 393f, availableHeightDp = 420f)

        assertEquals(152.08f, metrics.partnerCardHeightDp, 0.01f)
        assertEquals(108f, metrics.partnerEmojiSizeDp, 0.001f)
        assertEquals(2.48f, metrics.partnerContentGapDp, 0.01f)
    }

    @Test
    fun `uses narrower gutters on compact phones`() {
        val metrics = HomeLayout.metrics(availableWidthDp = 340f, availableHeightDp = 620f)

        assertEquals(12f, metrics.horizontalPaddingDp, 0.001f)
    }
}
