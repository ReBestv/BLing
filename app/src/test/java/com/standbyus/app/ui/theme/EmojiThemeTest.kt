package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.ThemeFeeling
import com.standbyus.app.data.model.ThemePack
import com.standbyus.app.data.model.ThemeSticker
import com.standbyus.app.data.remote.SupabaseConfig
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
            feelings = listOf(ThemeFeeling("happy", "开心", "happy.png")),
            stickers = emptyList()
        )

        assertEquals(
            "${SupabaseConfig.STORAGE_URL}themes/xiaoxin/happy.png",
            theme.stickerUrl("happy")
        )
    }

    @Test
    fun `theme sticker url includes themes bucket path`() {
        val theme = EmojiThemeSet(
            id = "vv",
            name = "VV",
            bucket = "VV",
            icon = "love_you.webp",
            feelings = emptyList(),
            stickers = listOf(ThemeSticker("eating", "吃饭", "eating.webp", listOf("doing")))
        )

        assertEquals(
            "${SupabaseConfig.STORAGE_URL}themes/VV/eating.webp",
            theme.themeStickerUrl("eating")
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
                    feelings = listOf(ThemeFeeling("happy", "开心", "happy.png")),
                    stickers = emptyList()
                )
            )
        )

        assertEquals(
            "${SupabaseConfig.STORAGE_URL}themes/xiaoxin/happy.png",
            EmojiThemeManager.themes.last().resolvedIcon
        )
    }
}
