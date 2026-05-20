package com.standbyus.app.ui.checkin

import kotlin.math.min

object CheckinLayout {
    const val NATURAL_INFO_WIDTH_DP = 366f
    const val NATURAL_INFO_HEIGHT_DP = 620f

    fun infoScale(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): Float {
        val widthScale = availableWidthDp / NATURAL_INFO_WIDTH_DP
        val heightScale = availableHeightDp / NATURAL_INFO_HEIGHT_DP
        return min(1f, min(widthScale, heightScale))
    }
}
