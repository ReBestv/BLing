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
}
