package com.standbyus.app.ui.home

enum class HomeStatusInteractionSection {
    LATEST_INTERACTION,
    MY_STATUS,
    INTERACTION_ACTIONS,
    INTERACTION_ERROR
}

object HomeStatusInteractionOrder {
    fun sections(hasInteractionError: Boolean): List<HomeStatusInteractionSection> =
        buildList {
            add(HomeStatusInteractionSection.LATEST_INTERACTION)
            add(HomeStatusInteractionSection.MY_STATUS)
            add(HomeStatusInteractionSection.INTERACTION_ACTIONS)
            if (hasInteractionError) {
                add(HomeStatusInteractionSection.INTERACTION_ERROR)
            }
        }
}
