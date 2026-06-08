package com.standbyus.app.ui.album

import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumLayoutTest {

    @Test
    fun `keeps album grid airy on regular phones`() {
        val metrics = AlbumLayout.metrics(availableWidthDp = 393f, availableHeightDp = 700f)

        assertEquals(24f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(8f, metrics.gridGapDp, 0.001f)
        assertEquals(160f, metrics.sheetPreviewSizeDp, 0.001f)
        assertEquals(20f, metrics.sheetPaddingDp, 0.001f)
    }

    @Test
    fun `tightens album spacing on compact phones`() {
        val metrics = AlbumLayout.metrics(availableWidthDp = 340f, availableHeightDp = 560f)

        assertEquals(16f, metrics.horizontalPaddingDp, 0.001f)
        assertEquals(6f, metrics.gridGapDp, 0.001f)
        assertEquals(132f, metrics.sheetPreviewSizeDp, 0.001f)
        assertEquals(16f, metrics.sheetPaddingDp, 0.001f)
    }
}
