package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.ThemePack
import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiThemeTest {

    @Test
    fun `sticker url includes themes bucket path`() {
        val theme = EmojiThemeSet(
            id = "xiaoxin",
            name = "小新",
            icon = "happy.png",
            isDefault = false,
            feelingNames = listOf("开心"),
            fileNameMap = mapOf("开心" to "happy"),
            bucket = "xiaoxin"
        )

        assertEquals(
            "https://dxwnnskelbygqdvjlorj.supabase.co/storage/v1/object/public/themes/xiaoxin/happy.png",
            theme.stickerUrl("开心")
        )
    }

    @Test
    fun `updateThemes builds theme icon under themes bucket`() {
        EmojiThemeManager.updateThemes(
            listOf(
                ThemePack(
                    id = "xiaoxin",
                    name = "小新",
                    bucket = "xiaoxin",
                    icon = "happy.png",
                    feelings = listOf("开心")
                )
            )
        )

        assertEquals(
            "https://dxwnnskelbygqdvjlorj.supabase.co/storage/v1/object/public/themes/xiaoxin/happy.png",
            EmojiThemeManager.themes.last().icon
        )
    }
}
