package com.standbyus.app.widget

import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.ui.home.HomePartnerStatusCardSurfaceStyle

object StandByWidgetStyle {
    val primaryTextColor = Color(0xFF5A4A42)
    val secondaryTextColor = Color(0xCC5A4A42)
    val tertiaryTextColor = Color(0x995A4A42)
    val emptyBackgroundColor = Color(0xFFFFF8F5)

    fun backgroundColorFor(feelingKey: String?): Color {
        val feeling = Feeling.fromKey(feelingKey.orEmpty())
        return HomePartnerStatusCardSurfaceStyle.gradientColorsFor(feeling)[1]
    }
}
