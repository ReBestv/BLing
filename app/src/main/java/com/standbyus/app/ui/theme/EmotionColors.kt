package com.standbyus.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.standbyus.app.data.model.Feeling

fun feelingBackgroundColor(feeling: Feeling, isDark: Boolean = false): Color {
    val base = when (feeling) {
        Feeling.HAPPY -> Color(0xFFFFD93D)
        Feeling.SAD -> Color(0xFF74B9FF)
        Feeling.TIRED -> Color(0xFFB2BEC3)
        Feeling.SICK -> Color(0xFFFF7675)
        Feeling.RELAXED -> Color(0xFF55EFC4)
        Feeling.MISSING -> Color(0xFFFD79A8)
        Feeling.SLEEPING -> Color(0xFFA29BFE)
        Feeling.HUSTLING -> Color(0xFF00CEC9)
        Feeling.THINKING -> Color(0xFFFFEAA7)
    }
    return if (isDark) base.copy(alpha = 0.7f) else base
}
