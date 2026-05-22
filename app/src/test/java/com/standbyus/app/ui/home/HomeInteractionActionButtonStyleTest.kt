package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeInteractionActionButtonStyleTest {

    @Test
    fun `keeps quick reply capsules compact with pale backgrounds`() {
        assertEquals(42f, HomeInteractionActionButtonStyle.heightDp, 0.001f)
        assertEquals(18f, HomeInteractionActionButtonStyle.cornerRadiusDp, 0.001f)
        assertEquals(Color(0xFFFFF0EC), HomeInteractionActionButtonStyle.containerColorForIndex(0))
        assertEquals(Color(0xFFF7F0FF), HomeInteractionActionButtonStyle.containerColorForIndex(1))
        assertEquals(Color(0xFFFFF7E2), HomeInteractionActionButtonStyle.containerColorForIndex(2))
        assertEquals(Color(0xFFF0F6FF), HomeInteractionActionButtonStyle.containerColorForIndex(3))
    }
}
