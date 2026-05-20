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
    fun `uses subtle translucent polish for card surface`() {
        assertEquals(Color(0x99FFFFFF), HomePartnerStatusCardSurfaceStyle.borderColor)
        assertEquals(Color(0x40FFFFFF), HomePartnerStatusCardSurfaceStyle.topGlowColor)
        assertEquals(Color(0x26FFD6C8), HomePartnerStatusCardSurfaceStyle.warmGlowColor)
    }
}
