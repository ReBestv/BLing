package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBackgroundStyleTest {

    @Test
    fun `uses a warm peach background gradient`() {
        assertEquals(
            listOf(
                Color(0xFFFFE8DE),
                Color(0xFFFFF5EF),
                Color(0xFFFFFAF6)
            ),
            HomeBackgroundStyle.gradientColors
        )
    }
}
