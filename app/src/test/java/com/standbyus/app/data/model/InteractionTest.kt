package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InteractionTest {

    @Test
    fun fromMapParsesCompleteInteraction() {
        val interaction = Interaction.fromMap(
            mapOf(
                "id" to 7L,
                "fromUserId" to "from",
                "toUserId" to "to",
                "type" to "hug",
                "text" to "抱抱一个",
                "targetStatusTime" to 123L,
                "createdAt" to 456L,
                "readAt" to 789L
            )
        )

        assertEquals(7L, interaction.id)
        assertEquals("from", interaction.fromUserId)
        assertEquals("to", interaction.toUserId)
        assertEquals("hug", interaction.type)
        assertEquals("抱抱一个", interaction.text)
        assertEquals(123L, interaction.targetStatusTime)
        assertEquals(456L, interaction.createdAt)
        assertEquals(789L, interaction.readAt)
    }

    @Test
    fun fromMapUsesSafeDefaultsForMissingFields() {
        val interaction = Interaction.fromMap(emptyMap())

        assertEquals(0L, interaction.id)
        assertEquals("", interaction.fromUserId)
        assertEquals("", interaction.toUserId)
        assertEquals("", interaction.type)
        assertEquals("", interaction.text)
        assertEquals(0L, interaction.targetStatusTime)
        assertEquals(0L, interaction.createdAt)
        assertNull(interaction.readAt)
    }

    @Test
    fun interactionTypeProvidesDisplayText() {
        assertEquals("TA想抱抱你", InteractionType.HUG.receivedText("TA"))
        assertEquals("TA也想你了", InteractionType.MISS_YOU_TOO.receivedText("TA"))
        assertEquals("不宁不宁在干嘛", InteractionType.NUDGE_UPDATE.sendText)
        assertEquals("TA正在拉屎", InteractionType.POOP_CHECKIN.receivedText("TA"))
        assertEquals("正在拉屎", InteractionType.POOP_CHECKIN.sendText)
        assertEquals(InteractionType.POOP_CHECKIN, InteractionType.fromKey("poop_checkin"))
    }
}
