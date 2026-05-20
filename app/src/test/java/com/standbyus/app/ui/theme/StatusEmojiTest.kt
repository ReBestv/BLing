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
    fun `falls back to saved fallback emoji when stored value is a url`() {
        assertEquals("😞", StatusEmoji.textFallback("https://example.com/upset.png", "upset", "😞"))
        assertEquals("🥰", StatusEmoji.textFallback("", "missing"))
        assertEquals("✨", StatusEmoji.textFallback("", ""))
    }
}
