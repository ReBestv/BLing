package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStatusBubbleStyleTest {

    @Test
    fun `uses soft backgrounds for interaction notice and my status`() {
        assertEquals(Color(0xFFFFF1ED), HomeStatusBubbleStyle.latestInteractionContainerColor)
        assertEquals(Color(0xFFFFFAF0), HomeStatusBubbleStyle.myStatusContainerColor)
        assertEquals(Color(0x33FFB4A2), HomeStatusBubbleStyle.borderColor)
    }
}
