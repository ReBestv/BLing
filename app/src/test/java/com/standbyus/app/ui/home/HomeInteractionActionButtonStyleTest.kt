package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeInteractionActionButtonStyleTest {

    @Test
    fun `keeps quick reply capsules compact with pale backgrounds`() {
        assertEquals(52f, HomeInteractionActionButtonStyle.heightDp, 0.001f)
        assertEquals(22f, HomeInteractionActionButtonStyle.cornerRadiusDp, 0.001f)
        assertEquals(Color(0xCCFFFFFF), HomeInteractionActionButtonStyle.containerColorForIndex(0))
        assertEquals(Color(0xE6FFF2ED), HomeInteractionActionButtonStyle.containerColorForIndex(1))
        assertEquals(Color(0xE6FFF7E2), HomeInteractionActionButtonStyle.containerColorForIndex(2))
        assertEquals(Color(0xE6F1F8F3), HomeInteractionActionButtonStyle.containerColorForIndex(3))
    }
}
