package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.ThemeFeeling
import com.standbyus.app.data.model.ThemeSticker
import com.standbyus.app.data.model.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiThemeSnapshotTest {

    @Test
    fun createsPublishSnapshotFromThemeFeeling() {
        val theme = EmojiThemeSet(
            id = "xiaoxin",
            name = "Xiaoxin",
            bucket = "xiaoxin",
            icon = "happy.png",
            feelings = listOf(ThemeFeeling("upset", "Upset", "upset.png")),
            stickers = emptyList()
        )

        val snapshot = EmojiThemeManager.createSnapshot(theme, "upset")

        assertEquals("xiaoxin", snapshot.themeId)
        assertEquals("Xiaoxin", snapshot.themeName)
        assertEquals("upset", snapshot.feelingKey)
        assertEquals("Upset", snapshot.feelingLabel)
        assertEquals(Feeling.UPSET.emoji, snapshot.feelingFallbackEmoji)
        assertTrue(snapshot.feelingAsset.endsWith("/storage/v1/object/public/themes/xiaoxin/upset.png"))
    }

    @Test
    fun infersMoodColorFromSelectedThemeStickerTags() {
        val theme = EmojiThemeSet(
            id = "emoji_motion",
            name = "Motion",
            bucket = "emoji-motion",
            icon = "heart.webp",
            feelings = emptyList(),
            stickers = listOf(ThemeSticker("heart", "Love", "heart.webp", listOf("love", "happy")))
        )

        val snapshot = EmojiThemeManager.createSnapshot(theme, "happy", "heart")

        assertEquals(Feeling.LOVE.key, snapshot.feelingKey)
        assertEquals(Feeling.LOVE.displayName, snapshot.feelingLabel)
        assertEquals(Feeling.LOVE.emoji, snapshot.feelingFallbackEmoji)
        assertEquals(Feeling.LOVE.color.toHex(), snapshot.feelingColor)
        assertEquals("heart", snapshot.stickerId)
        assertEquals("Love", snapshot.stickerLabel)
        assertTrue(snapshot.stickerAsset!!.endsWith("/storage/v1/object/public/themes/emoji-motion/heart.webp"))
        assertEquals(snapshot.stickerAsset, snapshot.feelingAsset)
    }

    @Test
    fun keepsActionOnlyThemeStickerNeutral() {
        val theme = EmojiThemeSet(
            id = "vv",
            name = "VV",
            bucket = "VV",
            icon = "love_you.webp",
            feelings = emptyList(),
            stickers = listOf(ThemeSticker("eating", "Eating", "eating.webp", listOf("doing")))
        )

        val snapshot = EmojiThemeManager.createSnapshot(theme, "happy", "eating")

        assertEquals("", snapshot.feelingKey)
        assertEquals("", snapshot.feelingLabel)
        assertEquals("", snapshot.feelingFallbackEmoji)
        assertEquals("#ffffe5dc", snapshot.feelingColor)
        assertEquals("eating", snapshot.stickerId)
        assertEquals("Eating", snapshot.stickerLabel)
        assertTrue(snapshot.stickerAsset!!.endsWith("/storage/v1/object/public/themes/VV/eating.webp"))
        assertEquals(snapshot.stickerAsset, snapshot.feelingAsset)
    }
}
