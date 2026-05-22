package com.standbyus.app.ui.poststatus

import com.standbyus.app.data.model.Feeling
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
}
