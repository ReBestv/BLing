package com.standbyus.app.ui.poststatus

import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.ThemeSticker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusPhraseSuggestionsTest {

    @Test
    fun everyFeelingHasPhraseSuggestions() {
        Feeling.entries.forEach { feeling ->
            val phrases = StatusPhraseSuggestions.forFeeling(feeling)

            assertTrue("${feeling.key} should have suggestions", phrases.isNotEmpty())
            assertTrue("${feeling.key} should have concise suggestions", phrases.all { it.length <= 12 })
        }
    }

    @Test
    fun unknownFeelingFallsBackToGeneralSuggestions() {
        val phrases = StatusPhraseSuggestions.forFeelingKey("unknown")

        assertTrue(phrases.isNotEmpty())
        assertTrue("在忙，晚点找你" in phrases)
    }

    @Test
    fun exactStickerSuggestionOverridesInferredFeeling() {
        val sticker = ThemeSticker(
            id = "eating",
            label = "吃饭",
            asset = "eating.webp",
            tags = listOf("love")
        )

        val phrases = StatusPhraseSuggestions.forSticker(sticker, Feeling.LOVE)

        assertEquals(listOf("在吃饭", "晚点找你", "给你拍一口"), phrases)
    }

    @Test
    fun stickerTagsCanDriveSuggestions() {
        val sticker = ThemeSticker(
            id = "custom_game",
            label = "游戏",
            asset = "game.webp",
            tags = listOf("gaming")
        )

        val phrases = StatusPhraseSuggestions.forSticker(sticker, inferredFeeling = null)

        assertEquals(listOf("在玩游戏", "打完找你", "一起玩吗"), phrases)
    }

    @Test
    fun unknownStickerFallsBackToInferredFeeling() {
        val sticker = ThemeSticker(
            id = "custom_sleep",
            label = "困了",
            asset = "sleep.webp",
            tags = emptyList()
        )

        val phrases = StatusPhraseSuggestions.forSticker(sticker, Feeling.SLEEPING)

        assertEquals(StatusPhraseSuggestions.forFeeling(Feeling.SLEEPING), phrases)
    }

    @Test
    fun unknownStickerWithoutFeelingFallsBackToGeneralSuggestions() {
        val sticker = ThemeSticker(
            id = "custom_blank",
            label = "随便",
            asset = "blank.webp",
            tags = emptyList()
        )

        val phrases = StatusPhraseSuggestions.forSticker(sticker, inferredFeeling = null)

        assertEquals(StatusPhraseSuggestions.fallbackPhrases(), phrases)
    }
}
