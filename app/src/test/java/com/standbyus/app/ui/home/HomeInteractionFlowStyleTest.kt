package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeInteractionFlowStyleTest {

    @Test
    fun `uses a soft continuous flow treatment below partner status`() {
        assertEquals(-8f, HomeInteractionFlowStyle.topOverlapDp, 0.001f)
        assertEquals(2f, HomeInteractionFlowStyle.horizontalInsetDp, 0.001f)
        assertEquals(18f, HomeInteractionFlowStyle.topPaddingDp, 0.001f)
        assertEquals(12f, HomeInteractionFlowStyle.horizontalPaddingDp, 0.001f)
        assertEquals(16f, HomeInteractionFlowStyle.bottomPaddingDp, 0.001f)
        assertEquals(10f, HomeInteractionFlowStyle.itemGapDp, 0.001f)
        assertEquals(24f, HomeInteractionFlowStyle.cornerRadiusDp, 0.001f)
        assertEquals(Color(0x85FFF8F4), HomeInteractionFlowStyle.topColor)
        assertEquals(Color(0x00FFFAF6), HomeInteractionFlowStyle.bottomColor)
    }
}
