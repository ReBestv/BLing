package com.standbyus.app.ui.checkin

import org.junit.Assert.assertEquals
import org.junit.Test

class CheckinIntervalFormatterTest {

    @Test
    fun `shows elapsed time since last checkin before the next checkin`() {
        val lastCheckin = 1_000L
        val now = lastCheckin + 15 * 3_600_000L + 17 * 60_000L

        assertEquals("15h17m", CheckinIntervalFormatter.sinceLastCheckin(lastCheckin, now))
    }

    @Test
    fun `shows minutes when less than one hour has elapsed`() {
        val lastCheckin = 1_000L
        val now = lastCheckin + 42 * 60_000L

        assertEquals("42m", CheckinIntervalFormatter.sinceLastCheckin(lastCheckin, now))
    }

    @Test
    fun `shows empty dash before first checkin`() {
        assertEquals("—", CheckinIntervalFormatter.sinceLastCheckin(null, now = 10_000L))
    }
}
