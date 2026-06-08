package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeInteractionFlowStyleTest {

    @Test
    fun `uses a soft continuous flow treatment below partner status`() {
        assertEquals(0f, HomeInteractionFlowStyle.topOverlapDp, 0.001f)
        assertEquals(10f, HomeInteractionFlowStyle.horizontalInsetDp, 0.001f)
        assertEquals(24f, HomeInteractionFlowStyle.topPaddingDp, 0.001f)
        assertEquals(13f, HomeInteractionFlowStyle.horizontalPaddingDp, 0.001f)
        assertEquals(14f, HomeInteractionFlowStyle.bottomPaddingDp, 0.001f)
        assertEquals(12f, HomeInteractionFlowStyle.itemGapDp, 0.001f)
        assertEquals(10f, HomeInteractionFlowStyle.actionGridGapDp, 0.001f)
        assertEquals(28f, HomeInteractionFlowStyle.cornerRadiusDp, 0.001f)
        assertEquals(286f, HomeInteractionFlowStyle.expandedContentHeightDp, 0.001f)
        assertEquals(Color(0xC4FFFFFF), HomeInteractionFlowStyle.topColor)
        assertEquals(Color(0x55FFFFFF), HomeInteractionFlowStyle.bottomColor)
    }
}
