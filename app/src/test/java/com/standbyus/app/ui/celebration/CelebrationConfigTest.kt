package com.standbyus.app.ui.celebration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CelebrationConfigTest {

    @Test
    fun birthdaysUseBirthdayStyleAndHighestPriority() {
        val firstBirthday = CelebrationConfig.match(3, 9)
        val secondBirthday = CelebrationConfig.match(6, 24)

        assertEquals("birthday_0309", firstBirthday?.id)
        assertEquals(CelebrationStyle.BIRTHDAY, firstBirthday?.style)
        assertEquals(100, firstBirthday?.priority)
        assertEquals("birthday_0624", secondBirthday?.id)
        assertEquals(CelebrationStyle.BIRTHDAY, secondBirthday?.style)
        assertEquals(100, secondBirthday?.priority)
    }

    @Test
    fun romanticDatesUseHeartsStyle() {
        assertEquals(CelebrationStyle.HEARTS, CelebrationConfig.match(2, 14)?.style)
        assertEquals(CelebrationStyle.HEARTS, CelebrationConfig.match(5, 20)?.style)
    }

    @Test
    fun matchChoosesHighestPriorityForSameDay() {
        val lowPriority = CelebrationDay(
            id = "low",
            month = 5,
            day = 20,
            emoji = "x",
            message = "low",
            style = CelebrationStyle.FIREWORKS,
            priority = 1
        )
        val highPriority = CelebrationDay(
            id = "high",
            month = 5,
            day = 20,
            emoji = "y",
            message = "high",
            style = CelebrationStyle.BIRTHDAY,
            priority = 99
        )

        assertEquals(highPriority, CelebrationConfig.match(5, 20, listOf(lowPriority, highPriority)))
    }

    @Test
    fun displayKeyIncludesIdAndDate() {
        assertEquals("shown_love_520_20260520", CelebrationConfig.displayKey("love_520", "20260520"))
        assertTrue(CelebrationConfig.displayKey("birthday_0309", "20260309").contains("birthday_0309"))
    }
}
