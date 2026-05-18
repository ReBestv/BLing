package com.standbyus.app.data.model

import androidx.compose.ui.graphics.Color

enum class Feeling(
    val displayName: String,
    val emoji: String,
    val color: Color,
    val gradientStart: Color,
    val gradientEnd: Color
) {
    HAPPY("开心", "😊",
        Color(0xFFFFD180), Color(0xFFFFD180), Color(0xFFFFAB40)),
    MISSING("想你", "🥰",
        Color(0xFFF8BBD0), Color(0xFFF8BBD0), Color(0xFFF48FB1)),
    KISS("亲亲", "😘",
        Color(0xFFE1BEE7), Color(0xFFE1BEE7), Color(0xFFCE93D8)),
    LOVE("爱你", "❤️",
        Color(0xFFEF9A9A), Color(0xFFEF9A9A), Color(0xFFE57373)),
    RELAXED("悠闲", "😌",
        Color(0xFFB2DFDB), Color(0xFFB2DFDB), Color(0xFF80CBC4)),
    HUSTLING("奋斗", "💪",
        Color(0xFFFFE0B2), Color(0xFFFFE0B2), Color(0xFFFFCC80)),
    THINKING("思考", "🤔",
        Color(0xFFFFF9C4), Color(0xFFFFF9C4), Color(0xFFFFF176)),
    WATCHING("追剧", "📺",
        Color(0xFFB3E5FC), Color(0xFFB3E5FC), Color(0xFF81D4FA)),
    SAD("难过", "😢",
        Color(0xFFBBDEFB), Color(0xFFBBDEFB), Color(0xFF90CAF9)),
    UPSET("沮丧", "😞",
        Color(0xFFD7CCC8), Color(0xFFD7CCC8), Color(0xFFBCAAA4)),
    ANGRY("生气", "😡",
        Color(0xFFFFCDD2), Color(0xFFFFCDD2), Color(0xFFEF9A9A)),
    ANXIOUS("焦虑", "😰",
        Color(0xFFE0E0E0), Color(0xFFE0E0E0), Color(0xFFBDBDBD)),
    TIRED("疲惫", "😫",
        Color(0xFFC8E6C9), Color(0xFFC8E6C9), Color(0xFFA5D6A7)),
    SICK("生病", "🤒",
        Color(0xFFDCEDC8), Color(0xFFDCEDC8), Color(0xFFC5E1A5)),
    BORED("无聊", "😑",
        Color(0xFFF0F4C3), Color(0xFFF0F4C3), Color(0xFFE6EE9C)),
    SLEEPING("睡觉", "😴",
        Color(0xFFE8EAF6), Color(0xFFE8EAF6), Color(0xFFC5CAE9));

    companion object {
        fun fromDisplayName(name: String): Feeling? = entries.find { it.displayName == name }
        fun fromEmoji(emoji: String): Feeling? = entries.find { it.emoji == emoji }
    }
}
