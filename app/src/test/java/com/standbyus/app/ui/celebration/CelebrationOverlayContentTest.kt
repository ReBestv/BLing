package com.standbyus.app.ui.celebration

import org.junit.Assert.assertEquals
import org.junit.Test

class CelebrationOverlayContentTest {

    @Test
    fun birthdayStyleUsesImageContent() {
        assertEquals(CelebrationOverlayContent.BIRTHDAY_IMAGE, CelebrationStyle.BIRTHDAY.overlayContent())
    }

    @Test
    fun nonBirthdayStylesKeepEmojiContent() {
        val nonBirthdayStyles = CelebrationStyle.entries - CelebrationStyle.BIRTHDAY

        nonBirthdayStyles.forEach { style ->
            assertEquals(CelebrationOverlayContent.EMOJI, style.overlayContent())
        }
    }
}
