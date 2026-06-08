package com.standbyus.app.ui.celebration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BirthdayGiftEffectSpecTest {

    @Test
    fun birthdayGiftEffectUsesSelectedPreviewElements() {
        val spec = CelebrationStyle.BIRTHDAY.giftEffectSpec()

        assertTrue(spec.enabled)
        assertTrue(spec.showCrown)
        assertTrue(spec.showGiftBox)
        assertFalse(spec.showMessage)
        assertEquals(34, spec.fallingStarCount)
        assertEquals(16, spec.floatingHeartCount)
        assertEquals(16, spec.ribbonCount)
        assertEquals(8, spec.burstRibbonCount)
        assertEquals(6, spec.burstSparkCount)
        assertEquals(5, spec.burstPetalCount)
        assertEquals(5, spec.starColors.size)
    }

    @Test
    fun nonBirthdayStylesDoNotUseGiftEffect() {
        val nonBirthdayStyles = CelebrationStyle.entries - CelebrationStyle.BIRTHDAY

        nonBirthdayStyles.forEach { style ->
            assertFalse(style.giftEffectSpec().enabled)
        }
    }
}
