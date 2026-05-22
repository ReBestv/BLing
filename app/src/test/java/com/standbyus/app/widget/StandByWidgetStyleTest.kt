package com.standbyus.app.widget

import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.ui.home.HomePartnerStatusCardSurfaceStyle
import org.junit.Assert.assertEquals
import org.junit.Test

class StandByWidgetStyleTest {

    @Test
    fun `uses home partner card palette for widget background`() {
        val homePalette = HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.MISSING)

        assertEquals(
            homePalette[1],
            StandByWidgetStyle.backgroundColorFor("missing")
        )
    }

    @Test
    fun `falls back to happy palette when feeling key is missing`() {
        assertEquals(
            HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.HAPPY)[1],
            StandByWidgetStyle.backgroundColorFor("")
        )
    }

    @Test
    fun `exposes widget text colors with enough contrast on soft mood backgrounds`() {
        assertEquals(Color(0xFF5A4A42), StandByWidgetStyle.primaryTextColor)
        assertEquals(Color(0xCC5A4A42), StandByWidgetStyle.secondaryTextColor)
    }
}
