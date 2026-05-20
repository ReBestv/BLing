package com.standbyus.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusEmojiTest {

    @Test
    fun `detects remote image urls`() {
        assertTrue(StatusEmoji.isRemoteImage("https://example.com/themes/xiaoxin/happy.png"))
        assertTrue(StatusEmoji.isRemoteImage("http://example.com/happy.webp"))
        assertFalse(StatusEmoji.isRemoteImage("😊"))
        assertFalse(StatusEmoji.isRemoteImage(""))
    }

    @Test
    fun `falls back to feeling emoji when stored value is a url`() {
        assertEquals("😊", StatusEmoji.textFallback("https://example.com/happy.png", "开心"))
        assertEquals("🥰", StatusEmoji.textFallback("", "想你"))
        assertEquals("✨", StatusEmoji.textFallback("", ""))
    }
}
