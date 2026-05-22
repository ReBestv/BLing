package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.ThemeFeeling
import com.standbyus.app.data.model.ThemeSticker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiThemeSnapshotTest {

    @Test
    fun createsPublishSnapshotFromThemeFeeling() {
        val theme = EmojiThemeSet(
            id = "xiaoxin",
            name = "小新",
            bucket = "xiaoxin",
            icon = "happy.png",
            feelings = listOf(ThemeFeeling("upset", "沮丧", "upset.png")),
            stickers = emptyList()
        )

        val snapshot = EmojiThemeManager.createSnapshot(theme, "upset")

        assertEquals("xiaoxin", snapshot.themeId)
        assertEquals("小新", snapshot.themeName)
        assertEquals("upset", snapshot.feelingKey)
        assertEquals("沮丧", snapshot.feelingLabel)
        assertEquals("😞", snapshot.feelingFallbackEmoji)
        assertTrue(snapshot.feelingAsset.endsWith("/storage/v1/object/public/themes/xiaoxin/upset.png"))
    }

    @Test
    fun createsPublishSnapshotFromSelectedThemeSticker() {
        val theme = EmojiThemeSet(
            id = "vv",
            name = "VV",
            bucket = "VV",
            icon = "love_you.webp",
            feelings = emptyList(),
            stickers = listOf(ThemeSticker("eating", "吃饭", "eating.webp", listOf("doing")))
        )

        val snapshot = EmojiThemeManager.createSnapshot(theme, "happy", "eating")

        assertEquals("happy", snapshot.feelingKey)
        assertEquals("开心", snapshot.feelingLabel)
        assertEquals("eating", snapshot.stickerId)
        assertEquals("吃饭", snapshot.stickerLabel)
        assertTrue(snapshot.stickerAsset!!.endsWith("/storage/v1/object/public/themes/VV/eating.webp"))
        assertEquals(snapshot.stickerAsset, snapshot.feelingAsset)
    }
}
