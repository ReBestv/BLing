package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.Feeling
import org.junit.Assert.assertEquals
import org.junit.Test

class HomePartnerStatusCardSurfaceStyleTest {

    @Test
    fun `uses layered mist blue palette for sad status card`() {
        assertEquals(
            listOf(
                Color(0xFFE8F6FF),
                Color(0xFFB8DDF1),
                Color(0xFFDCD7FA)
            ),
            HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.SAD)
        )
    }

    @Test
    fun `uses polished palettes for every feeling group`() {
        assertEquals(
            listOf(
                Color(0xFFFFEFF4),
                Color(0xFFFFB9CA),
                Color(0xFFFFD9C8)
            ),
            HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.HAPPY)
        )
        assertEquals(
            listOf(
                Color(0xFFFFF0EA),
                Color(0xFFFFB8B8),
                Color(0xFFFFD1B5)
            ),
            HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.ANGRY)
        )
        assertEquals(
            listOf(
                Color(0xFFF8F1FF),
                Color(0xFFD9C3F3),
                Color(0xFFC7E6F4)
            ),
            HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.TIRED)
        )
        assertEquals(
            listOf(
                Color(0xFFFFF4DD),
                Color(0xFFFFD2A1),
                Color(0xFFFFE6CE)
            ),
            HomePartnerStatusCardSurfaceStyle.gradientColorsFor(Feeling.RELAXED)
        )
    }

    @Test
    fun `uses subtle translucent polish for card surface`() {
        assertEquals(Color(0x99FFFFFF), HomePartnerStatusCardSurfaceStyle.borderColor)
        assertEquals(Color(0x40FFFFFF), HomePartnerStatusCardSurfaceStyle.topGlowColor)
        assertEquals(Color(0x26FFD6C8), HomePartnerStatusCardSurfaceStyle.warmGlowColor)
    }
}
