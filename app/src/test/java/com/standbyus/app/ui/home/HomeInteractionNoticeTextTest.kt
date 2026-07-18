package com.standbyus.app.ui.home

import com.standbyus.app.data.model.Interaction
import com.standbyus.app.data.model.InteractionType
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeInteractionNoticeTextTest {

    @Test
    fun `uses partner nickname in latest interaction text`() {
        val interaction = Interaction(type = InteractionType.HARD_WORK.key)

        assertEquals(
            "小周想你啦",
            HomeInteractionNoticeText.resolve(
                interaction = interaction,
                partnerDisplayName = "小周"
            )
        )
    }

    @Test
    fun `falls back to partner label when nickname is blank`() {
        val interaction = Interaction(type = InteractionType.HUG.key)

        assertEquals(
            "对方想抱抱你",
            HomeInteractionNoticeText.resolve(
                interaction = interaction,
                partnerDisplayName = ""
            )
        )
    }
}
