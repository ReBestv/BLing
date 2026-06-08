package com.standbyus.app.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryLayoutTest {

    @Test
    fun `keeps timeline roomy on regular phones`() {
        val metrics = HistoryLayout.metrics(availableWidthDp = 393f, availableHeightDp = 700f)

        assertEquals(16f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(260f, metrics.partnerBubbleMaxWidthDp, 0.001f)
        assertEquals(16f, metrics.bubbleVerticalPaddingDp, 0.001f)
        assertEquals(40f, metrics.dotSizeDp, 0.001f)
    }

    @Test
    fun `tightens timeline on narrow phones`() {
        val metrics = HistoryLayout.metrics(availableWidthDp = 340f, availableHeightDp = 620f)

        assertEquals(12f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(228f, metrics.partnerBubbleMaxWidthDp, 0.001f)
        assertEquals(14f, metrics.bubbleVerticalPaddingDp, 0.001f)
        assertEquals(36f, metrics.dotSizeDp, 0.001f)
    }
}
