package com.standbyus.app.ui.home

import com.standbyus.app.data.model.Interaction

object HomeInteractionNoticeText {
    fun resolve(
        interaction: Interaction,
        partnerDisplayName: String
    ): String {
        val senderName = partnerDisplayName.ifBlank { "对方" }
        return interaction.displayText(senderName)
    }
}
