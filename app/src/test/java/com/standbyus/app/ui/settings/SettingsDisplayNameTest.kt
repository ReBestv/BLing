package com.standbyus.app.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsDisplayNameTest {

    @Test
    fun `uses nickname before remote partner name`() {
        assertEquals(
            "宝贝",
            SettingsDisplayName.resolvePartnerDisplayName(
                nickname = " 宝贝 ",
                partnerName = "小明"
            )
        )
    }

    @Test
    fun `uses partner name when nickname is blank`() {
        assertEquals(
            "小明",
            SettingsDisplayName.resolvePartnerDisplayName(
                nickname = "",
                partnerName = " 小明 "
            )
        )
    }

    @Test
    fun `does not expose theme icon urls as partner names`() {
        assertEquals(
            "对方",
            SettingsDisplayName.resolvePartnerDisplayName(
                nickname = "",
                partnerName = "https://example.supabase.co/storage/v1/object/public/themes/icon.png"
            )
        )
    }
}
