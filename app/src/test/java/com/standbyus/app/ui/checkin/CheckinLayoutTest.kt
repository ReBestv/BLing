package com.standbyus.app.ui.checkin

import org.junit.Assert.assertEquals
import org.junit.Test

class CheckinLayoutTest {

    @Test
    fun `keeps information cards at natural size when space is enough`() {
        assertEquals(
            1f,
            CheckinLayout.infoScale(availableWidthDp = 393f, availableHeightDp = 700f),
            0.001f
        )
    }

    @Test
    fun `reserves enough natural height for both statistic rows`() {
        assertEquals(620f, CheckinLayout.NATURAL_INFO_HEIGHT_DP, 0.001f)
    }

    @Test
    fun `scales all information cards down proportionally on short screens`() {
        assertEquals(
            0.645f,
            CheckinLayout.infoScale(availableWidthDp = 393f, availableHeightDp = 400f),
            0.001f
        )
    }

    @Test
    fun `scales information cards down proportionally on narrow screens`() {
        assertEquals(
            0.875f,
            CheckinLayout.infoScale(availableWidthDp = 320f, availableHeightDp = 620f),
            0.001f
        )
    }

    @Test
    fun `uses wider cards on regular phones`() {
        val metrics = CheckinLayout.metrics(availableWidthDp = 393f, availableHeightDp = 700f)

        assertEquals(353.7f, metrics.cardWidthDp, 0.001f)
    }

    @Test
    fun `keeps comfortable gutters on narrow phones`() {
        val metrics = CheckinLayout.metrics(availableWidthDp = 320f, availableHeightDp = 620f)

        assertEquals(288f, metrics.cardWidthDp, 0.001f)
    }

    @Test
    fun `grows the checkin button on tall screens`() {
        val metrics = CheckinLayout.metrics(availableWidthDp = 393f, availableHeightDp = 860f)

        assertEquals(118f, metrics.buttonSizeDp, 0.001f)
    }

    @Test
    fun `keeps the checkin button compact on short screens`() {
        val metrics = CheckinLayout.metrics(availableWidthDp = 393f, availableHeightDp = 560f)

        assertEquals(100f, metrics.buttonSizeDp, 0.001f)
    }

    @Test
    fun `keeps button medium when card area still needs room`() {
        val metrics = CheckinLayout.metrics(availableWidthDp = 393f, availableHeightDp = 760f)

        assertEquals(108f, metrics.buttonSizeDp, 0.001f)
    }

    @Test
    fun `reduces statistic card padding to keep the third row visible`() {
        val metrics = CheckinLayout.metrics(availableWidthDp = 393f, availableHeightDp = 760f)

        assertEquals(18f, metrics.statVerticalPaddingDp, 0.001f)
    }
}
