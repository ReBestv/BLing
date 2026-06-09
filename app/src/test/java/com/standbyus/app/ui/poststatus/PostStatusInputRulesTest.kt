package com.standbyus.app.ui.poststatus

import org.junit.Assert.assertEquals
import org.junit.Test

class PostStatusInputRulesTest {

    @Test
    fun `caps detail input at thirty characters`() {
        val input = "123456789012345678901234567890extra"

        val sanitized = PostStatusInputRules.sanitizeDetailInput(input)

        assertEquals(30, sanitized.length)
        assertEquals("123456789012345678901234567890", sanitized)
    }

    @Test
    fun `trims note text before publish`() {
        assertEquals("刚刚忙完", PostStatusInputRules.publishNote("  刚刚忙完  "))
    }
}
