package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePackTest {

    @Test
    fun parsesVersion2ThemeFeelingsByKey() {
        val manifest = ThemeManifest.fromJson(
            """
            {
              "version": 2,
              "themes": [
                {
                  "id": "xiaoxin",
                  "name": "小新",
                  "bucket": "xiaoxin",
                  "icon": "happy.png",
                  "feelings": [
                    { "key": "happy", "label": "开心", "asset": "happy.png" },
                    { "key": "upset", "label": "沮丧", "asset": "upset.png" }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        val theme = manifest.themes.single()

        assertEquals(2, manifest.version)
        assertEquals("沮丧", theme.feelingByKey("upset")?.label)
        assertEquals("upset.png", theme.fileName("upset"))
    }

    @Test
    fun parsesVersion3ThemeStickers() {
        val manifest = ThemeManifest.fromJson(
            """
            {
              "version": 3,
              "themes": [
                {
                  "id": "vv",
                  "name": "VV",
                  "bucket": "VV",
                  "icon": "love_you.webp",
                  "stickers": [
                    { "id": "eating", "label": "吃饭", "asset": "eating.webp", "tags": ["doing"] },
                    { "id": "coffee", "label": "喝咖啡", "asset": "coffee.webp", "tags": ["relaxed", "doing"] }
                  ],
                  "feelings": []
                }
              ]
            }
            """.trimIndent()
        )

        val theme = manifest.themes.single()

        assertEquals(3, manifest.version)
        assertEquals("吃饭", theme.stickerById("eating")?.label)
        assertEquals("coffee.webp", theme.stickerById("coffee")?.asset)
        assertEquals(listOf("relaxed", "doing"), theme.stickerById("coffee")?.tags)
    }
}
