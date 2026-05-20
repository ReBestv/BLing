package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.ThemeFeeling
import com.standbyus.app.data.model.ThemePack
import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiThemeTest {

    @Test
    fun `sticker url includes themes bucket path`() {
        val theme = EmojiThemeSet(
            id = "xiaoxin",
            name = "小新",
            bucket = "xiaoxin",
            icon = "happy.png",
            feelings = listOf(ThemeFeeling("happy", "开心", "happy.png"))
        )

        assertEquals(
            "https://dxwnnskelbygqdvjlorj.supabase.co/storage/v1/object/public/themes/xiaoxin/happy.png",
            theme.stickerUrl("happy")
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
                    feelings = listOf(ThemeFeeling("happy", "开心", "happy.png"))
                )
            )
        )

        assertEquals(
            "https://dxwnnskelbygqdvjlorj.supabase.co/storage/v1/object/public/themes/xiaoxin/happy.png",
            EmojiThemeManager.themes.last().resolvedIcon
        )
    }
}
