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
        assertTrue(StatusEmoji.isRemoteImage("https://example.com/love.gif"))
        assertTrue(StatusEmoji.isRemoteImage("https://example.com/love.heif?download=1"))
        assertFalse(StatusEmoji.isRemoteImage("😊"))
        assertFalse(StatusEmoji.isRemoteImage(""))
        assertFalse(StatusEmoji.isRemoteImage("https://example.com/love.mov"))
    }

    @Test
    fun `falls back to saved fallback emoji when stored value is a remote asset`() {
        assertEquals("😥", StatusEmoji.textFallback("https://example.com/upset.png", "upset", "😥"))
        assertEquals("😴", StatusEmoji.textFallback("https://example.com/sleeping.mov", "sleeping", "😴"))
        assertEquals("🥰", StatusEmoji.textFallback("", "missing"))
        assertEquals("✨", StatusEmoji.textFallback("", ""))
    }
}
