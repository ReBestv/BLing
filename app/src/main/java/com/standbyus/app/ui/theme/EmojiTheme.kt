package com.standbyus.app.ui.theme

import android.content.Context

data class EmojiThemeSet(
    val id: String,
    val name: String,
    val icon: String,
    val emojis: Map<String, String>
)

object EmojiThemeManager {
    private const val PREFS_NAME = "emoji_theme"
    private const val KEY_THEME_ID = "theme_id"

    val themes = listOf(
        EmojiThemeSet("default", "默认表情", "😊", mapOf(
            "开心" to "😊", "想你" to "🥰", "亲亲" to "😘", "爱你" to "❤️",
            "悠闲" to "😌", "奋斗" to "💪", "思考" to "🤔", "追剧" to "📺",
            "难过" to "😢", "沮丧" to "😞", "生气" to "😡", "焦虑" to "😰",
            "疲惫" to "😫", "生病" to "🤒", "无聊" to "😑", "睡觉" to "😴"
        )),
        EmojiThemeSet("cats", "猫猫主题", "🐱", mapOf(
            "开心" to "😺", "想你" to "😻", "亲亲" to "😽", "爱你" to "😻",
            "悠闲" to "😸", "奋斗" to "😼", "思考" to "🤔", "追剧" to "🙀",
            "难过" to "😿", "沮丧" to "😾", "生气" to "😾", "焦虑" to "🙀",
            "疲惫" to "😴", "生病" to "😿", "无聊" to "😹", "睡觉" to "😴"
        )),
        EmojiThemeSet("food", "食物主题", "🍔", mapOf(
            "开心" to "🍭", "想你" to "🍓", "亲亲" to "🍬", "爱你" to "🍒",
            "悠闲" to "🍵", "奋斗" to "🥡", "思考" to "🍳", "追剧" to "🍿",
            "难过" to "🍦", "沮丧" to "🍂", "生气" to "🧅", "焦虑" to "🍺",
            "疲惫" to "☕", "生病" to "🍯", "无聊" to "🥤", "睡觉" to "🥛"
        ))
    )

    private val defaultTheme: EmojiThemeSet get() = themes.first()

    fun getCurrentTheme(context: Context): EmojiThemeSet {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_THEME_ID, defaultTheme.id) ?: defaultTheme.id
        return themes.find { it.id == id } ?: defaultTheme
    }

    fun setCurrentTheme(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_ID, themeId).apply()
    }

    fun getEmoji(context: Context, feelingName: String): String {
        val theme = getCurrentTheme(context)
        return theme.emojis[feelingName] ?: defaultTheme.emojis[feelingName] ?: "😊"
    }
}
