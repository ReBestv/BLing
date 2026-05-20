package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.Feeling

object HomePartnerStatusCardSurfaceStyle {
    val borderColor = Color(0x99FFFFFF)
    val topGlowColor = Color(0x40FFFFFF)
    val warmGlowColor = Color(0x26FFD6C8)

    fun gradientColorsFor(feeling: Feeling): List<Color> = when (feeling) {
        Feeling.HAPPY, Feeling.LOVE, Feeling.KISS, Feeling.MISSING ->
            listOf(Color(0xFFFFDCE5), Color(0xFFFFB8C7), Color(0xFFFFD8C8))

        Feeling.SAD, Feeling.SICK, Feeling.UPSET, Feeling.ANXIOUS, Feeling.CRYING ->
            listOf(Color(0xFFE8F6FF), Color(0xFFB8DDF1), Color(0xFFDCD7FA))

        Feeling.ANGRY ->
            listOf(Color(0xFFFFE5E0), Color(0xFFFFBFC0), Color(0xFFFFD5C2))

        Feeling.TIRED, Feeling.SLEEPING ->
            listOf(Color(0xFFF4ECFF), Color(0xFFD8C8EF), Color(0xFFC9E4F2))

        Feeling.RELAXED, Feeling.HUSTLING, Feeling.THINKING, Feeling.WATCHING, Feeling.BORED ->
            listOf(Color(0xFFFFF0D9), Color(0xFFFFD3AA), Color(0xFFFFE4C9))
    }
}
