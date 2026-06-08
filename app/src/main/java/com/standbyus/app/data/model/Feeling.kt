package com.standbyus.app.data.model

import androidx.compose.ui.graphics.Color

enum class Feeling(
    val key: String,
    val displayName: String,
    val emoji: String,
    val color: Color,
    val gradientStart: Color,
    val gradientEnd: Color
) {
    HAPPY("happy", "开心", "😊",
        Color(0xFFFFD180), Color(0xFFFFD180), Color(0xFFFFAB40)),
    MISSING("missing", "想你", "🥰",
        Color(0xFFF8BBD0), Color(0xFFF8BBD0), Color(0xFFF48FB1)),
    KISS("kiss", "亲亲", "😘",
        Color(0xFFE1BEE7), Color(0xFFE1BEE7), Color(0xFFCE93D8)),
    LOVE("love", "爱你", "❤️",
        Color(0xFFEF9A9A), Color(0xFFEF9A9A), Color(0xFFE57373)),
    RELAXED("relaxed", "悠闲", "😌",
        Color(0xFFB2DFDB), Color(0xFFB2DFDB), Color(0xFF80CBC4)),
    HUSTLING("hustling", "奋斗", "💪",
        Color(0xFFFFE0B2), Color(0xFFFFE0B2), Color(0xFFFFCC80)),
    THINKING("thinking", "思考", "🤔",
        Color(0xFFFFF9C4), Color(0xFFFFF9C4), Color(0xFFFFF176)),
    WATCHING("watching", "追剧", "📺",
        Color(0xFFB3E5FC), Color(0xFFB3E5FC), Color(0xFF81D4FA)),
    SAD("sad", "难过", "😢",
        Color(0xFFBBDEFB), Color(0xFFBBDEFB), Color(0xFF90CAF9)),
    UPSET("upset", "沮丧", "😞",
        Color(0xFFD7CCC8), Color(0xFFD7CCC8), Color(0xFFBCAAA4)),
    ANGRY("angry", "生气", "😡",
        Color(0xFFFFCDD2), Color(0xFFFFCDD2), Color(0xFFEF9A9A)),
    ANXIOUS("anxious", "焦虑", "😰",
        Color(0xFFE0E0E0), Color(0xFFE0E0E0), Color(0xFFBDBDBD)),
    TIRED("tired", "疲惫", "😫",
        Color(0xFFC8E6C9), Color(0xFFC8E6C9), Color(0xFFA5D6A7)),
    SICK("sick", "生病", "🤒",
        Color(0xFFDCEDC8), Color(0xFFDCEDC8), Color(0xFFC5E1A5)),
    BORED("bored", "无聊", "😑",
        Color(0xFFF0F4C3), Color(0xFFF0F4C3), Color(0xFFE6EE9C)),
    SLEEPING("sleeping", "睡觉", "😴",
        Color(0xFFE8EAF6), Color(0xFFE8EAF6), Color(0xFFC5CAE9));

    companion object {
        fun fromKey(key: String): Feeling? = entries.find { it.key == key }
        fun fromDisplayName(name: String): Feeling? = entries.find { it.displayName == name }
        fun fromEmoji(emoji: String): Feeling? = entries.find { it.emoji == emoji }
        fun fromLegacyLabel(name: String): Feeling? = legacyLabelMap[name] ?: fromDisplayName(name)

        private val legacyLabelMap = mapOf(
            "开心" to HAPPY,
            "想你" to MISSING,
            "想你了" to MISSING,
            "亲亲" to KISS,
            "爱你" to LOVE,
            "爱心" to LOVE,
            "悠闲" to RELAXED,
            "奋斗" to HUSTLING,
            "思考" to THINKING,
            "追剧" to WATCHING,
            "看剧" to WATCHING,
            "难过" to SAD,
            "委屈" to UPSET,
            "沮丧" to UPSET,
            "生气" to ANGRY,
            "焦虑" to ANXIOUS,
            "疲惫" to TIRED,
            "生病" to SICK,
            "无聊" to BORED,
            "哭哭" to UPSET,
            "睡觉" to SLEEPING
        )
    }
}
