package com.standbyus.app.notification

import com.standbyus.app.data.model.Interaction
import com.standbyus.app.data.model.InteractionType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerEventNotificationGateTest {

    @Test
    fun notifiesOnlyUnreadPoopCheckinsOnce() {
        val gate = PartnerEventNotificationGate()
        val interaction = Interaction(
            id = 3L,
            type = InteractionType.POOP_CHECKIN.key,
            createdAt = 100L
        )

        assertTrue(gate.shouldNotify(interaction))
        assertFalse(gate.shouldNotify(interaction))
    }

    @Test
    fun ignoresReadOrNonPoopInteractions() {
        val gate = PartnerEventNotificationGate()

        assertFalse(
            gate.shouldNotify(
                Interaction(id = 4L, type = InteractionType.HUG.key, createdAt = 100L)
            )
        )
        assertFalse(
            gate.shouldNotify(
                Interaction(
                    id = 5L,
                    type = InteractionType.POOP_CHECKIN.key,
                    createdAt = 100L,
                    readAt = 200L
                )
            )
        )
        assertFalse(gate.shouldNotify(null))
    }

    @Test
    fun usesCreatedAtWhenRemoteIdIsMissing() {
        val gate = PartnerEventNotificationGate()
        val interaction = Interaction(
            id = 0L,
            type = InteractionType.POOP_CHECKIN.key,
            createdAt = 200L
        )

        assertTrue(gate.shouldNotify(interaction))
        assertFalse(gate.shouldNotify(interaction.copy(createdAt = 200L)))
        assertTrue(gate.shouldNotify(interaction.copy(createdAt = 201L)))
    }

    @Test
    fun restoresPreviouslyNotifiedKey() {
        val gate = PartnerEventNotificationGate(initialLastNotifiedKey = 7L)

        assertFalse(
            gate.shouldNotify(
                Interaction(id = 7L, type = InteractionType.POOP_CHECKIN.key, createdAt = 100L)
            )
        )
        assertTrue(
            gate.shouldNotify(
                Interaction(id = 8L, type = InteractionType.POOP_CHECKIN.key, createdAt = 101L)
            )
        )
        assertTrue(gate.lastNotifiedKey == 8L)
    }
}
