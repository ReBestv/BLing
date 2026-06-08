package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PairingInfoTest {

    @Test
    fun `round trips pair records for two devices`() {
        val pair = PairingInfo(
            pairId = "A2B3C4",
            user1Id = "device-a",
            user2Id = "device-b",
            user1Name = "我",
            user2Name = "对方"
        )

        assertEquals(pair, PairingInfo.fromMap(pair.toMap()))
    }

    @Test
    fun `uses safe defaults when remote pair fields are missing`() {
        assertEquals(PairingInfo(pairId = "A2B3C4"), PairingInfo.fromMap(mapOf("pairId" to "A2B3C4")))
    }
}
