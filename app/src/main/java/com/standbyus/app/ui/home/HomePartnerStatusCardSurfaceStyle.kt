package com.standbyus.app.ui.home

import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.Feeling

object HomePartnerStatusCardSurfaceStyle {
    val borderColor = Color(0x99FFFFFF)
    val topGlowColor = Color(0x40FFFFFF)
    val warmGlowColor = Color(0x26FFD6C8)

    fun gradientColorsFor(feeling: Feeling): List<Color> = when (feeling) {
        Feeling.HAPPY, Feeling.LOVE, Feeling.KISS, Feeling.MISSING ->
            listOf(Color(0xFFFFEFF4), Color(0xFFFFB9CA), Color(0xFFFFD9C8))

        Feeling.SAD, Feeling.SICK, Feeling.UPSET, Feeling.ANXIOUS ->
            listOf(Color(0xFFE8F6FF), Color(0xFFB8DDF1), Color(0xFFDCD7FA))

        Feeling.ANGRY ->
            listOf(Color(0xFFFFF0EA), Color(0xFFFFB8B8), Color(0xFFFFD1B5))

        Feeling.TIRED, Feeling.SLEEPING ->
            listOf(Color(0xFFF8F1FF), Color(0xFFD9C3F3), Color(0xFFC7E6F4))

        Feeling.RELAXED, Feeling.HUSTLING, Feeling.THINKING, Feeling.WATCHING, Feeling.BORED ->
            listOf(Color(0xFFFFF4DD), Color(0xFFFFD2A1), Color(0xFFFFE6CE))
    }
}
