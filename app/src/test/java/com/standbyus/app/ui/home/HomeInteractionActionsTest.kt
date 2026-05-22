package com.standbyus.app.ui.home

import com.standbyus.app.data.model.InteractionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HomeInteractionActionsTest {

    @Test
    fun quickActionsExcludePoopCheckinNotificationEvent() {
        assertEquals(
            listOf(
                InteractionType.HUG,
                InteractionType.MISS_YOU_TOO,
                InteractionType.HARD_WORK,
                InteractionType.NUDGE_UPDATE
            ),
            HomeInteractionActions.quickActions
        )
        assertFalse(HomeInteractionActions.quickActions.contains(InteractionType.POOP_CHECKIN))
    }
}
