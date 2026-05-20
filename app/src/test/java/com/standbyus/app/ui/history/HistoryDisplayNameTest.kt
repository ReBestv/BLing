package com.standbyus.app.ui.history

import org.junit.Assert.assertEquals
import org.junit.Test

class HistoryDisplayNameTest {

    @Test
    fun `keeps my timeline name unchanged`() {
        assertEquals(
            "我",
            HistoryDisplayName.resolveEntryDisplayName(
                isMe = true,
                partnerDisplayName = "宝贝"
            )
        )
    }

    @Test
    fun `uses partner nickname for partner timeline name`() {
        assertEquals(
            "宝贝",
            HistoryDisplayName.resolveEntryDisplayName(
                isMe = false,
                partnerDisplayName = " 宝贝 "
            )
        )
    }

    @Test
    fun `falls back when partner timeline name is blank`() {
        assertEquals(
            "对方",
            HistoryDisplayName.resolveEntryDisplayName(
                isMe = false,
                partnerDisplayName = ""
            )
        )
    }
}
