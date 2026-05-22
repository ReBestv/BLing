package com.standbyus.app.notification

import com.standbyus.app.data.model.Interaction
import com.standbyus.app.data.model.InteractionType

class PartnerEventNotificationGate(
    initialLastNotifiedKey: Long? = null
) {
    var lastNotifiedKey: Long? = initialLastNotifiedKey
        private set

    fun shouldNotify(interaction: Interaction?): Boolean {
        if (interaction == null) return false
        if (interaction.type != InteractionType.POOP_CHECKIN.key) return false
        if (interaction.readAt != null) return false

        val key = if (interaction.id > 0L) interaction.id else interaction.createdAt
        if (key <= 0L || key == lastNotifiedKey) return false

        lastNotifiedKey = key
        return true
    }
}
