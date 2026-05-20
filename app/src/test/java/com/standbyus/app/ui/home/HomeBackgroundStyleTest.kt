package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBackgroundStyleTest {

    @Test
    fun `uses warm peach to soft lavender background colors`() {
        assertEquals(
            listOf(
                Color(0xFFFFF1EC),
                Color(0xFFFFFAF7),
                Color(0xFFF5F3FF)
            ),
            HomeBackgroundStyle.gradientColors
        )
    }
}
