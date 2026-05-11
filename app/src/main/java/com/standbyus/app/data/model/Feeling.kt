package com.standbyus.app.data.model

import androidx.compose.ui.graphics.Color

enum class Feeling(
    val displayName: String,
    val emoji: String,
    val color: Color,
    val gradientStart: Color,
    val gradientEnd: Color
) {
    HAPPY("开心", "😊", Color(0xFFFFD93D), Color(0xFFFFD93D), Color(0xFFFF9F43)),
    SAD("难过", "😢", Color(0xFF74B9FF), Color(0xFF74B9FF), Color(0xFFA29BFE)),
    TIRED("疲惫", "😫", Color(0xFFB2BEC3), Color(0xFFB2BEC3), Color(0xFF636E72)),
    SICK("生病", "🤒", Color(0xFFFF7675), Color(0xFFFF7675), Color(0xFFFAB1A0)),
    RELAXED("悠闲", "😌", Color(0xFF55EFC4), Color(0xFF55EFC4), Color(0xFF00B894)),
    MISSING("想你了", "🥰", Color(0xFFFD79A8), Color(0xFFFD79A8), Color(0xFFE84393)),
    SLEEPING("睡觉", "😴", Color(0xFFA29BFE), Color(0xFFA29BFE), Color(0xFF6C5CE7)),
    HUSTLING("奋斗", "💪", Color(0xFF00CEC9), Color(0xFF00CEC9), Color(0xFF0984E3)),
    THINKING("思考", "🤔", Color(0xFFFFEAA7), Color(0xFFFFEAA7), Color(0xFFFDCB6E));

    companion object {
        fun fromDisplayName(name: String): Feeling? = entries.find { it.displayName == name }
        fun fromEmoji(emoji: String): Feeling? = entries.find { it.emoji == emoji }
    }
}
