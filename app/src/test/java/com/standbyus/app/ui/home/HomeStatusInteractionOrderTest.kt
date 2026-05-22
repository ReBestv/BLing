package com.standbyus.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStatusInteractionOrderTest {

    @Test
    fun `places my status between latest interaction and quick replies`() {
        assertEquals(
            listOf(
                HomeStatusInteractionSection.LATEST_INTERACTION,
                HomeStatusInteractionSection.MY_STATUS,
                HomeStatusInteractionSection.INTERACTION_ACTIONS
            ),
            HomeStatusInteractionOrder.sections(hasInteractionError = false)
        )
    }

    @Test
    fun `keeps interaction error after quick replies`() {
        assertEquals(
            listOf(
                HomeStatusInteractionSection.LATEST_INTERACTION,
                HomeStatusInteractionSection.MY_STATUS,
                HomeStatusInteractionSection.INTERACTION_ACTIONS,
                HomeStatusInteractionSection.INTERACTION_ERROR
            ),
            HomeStatusInteractionOrder.sections(hasInteractionError = true)
        )
    }
}
