package com.standbyus.app.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsLayoutTest {

    @Test
    fun `keeps settings cards airy on regular phones`() {
        val metrics = SettingsLayout.metrics(availableWidthDp = 393f, availableHeightDp = 700f)

        assertEquals(16f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(20f, metrics.cardPaddingDp, 0.001f)
        assertEquals(64f, metrics.profileAvatarSizeDp, 0.001f)
        assertEquals(400f, metrics.avatarPickerMaxHeightDp, 0.001f)
    }

    @Test
    fun `compacts settings cards on short or narrow phones`() {
        val metrics = SettingsLayout.metrics(availableWidthDp = 340f, availableHeightDp = 560f)

        assertEquals(12f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(16f, metrics.cardPaddingDp, 0.001f)
        assertEquals(56f, metrics.profileAvatarSizeDp, 0.001f)
        assertEquals(320f, metrics.avatarPickerMaxHeightDp, 0.001f)
    }
}
