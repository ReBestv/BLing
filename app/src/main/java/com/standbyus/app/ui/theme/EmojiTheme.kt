package com.standbyus.app.ui.theme

import android.content.Context
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.ThemePack
import com.standbyus.app.data.remote.SupabaseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 单个主题的 UI 表示。合并了远端 ThemePack 和本地 emoji map。
 */
data class EmojiThemeSet(
    val id: String,
    val name: String,
    val icon: String?,          // 图标 URL 或 emoji（default 主题用 emoji）
    val isDefault: Boolean,     // true = 内置默认主题（纯 emoji，无图片）
    val feelingNames: List<String>,  // 该主题覆盖的心情 displayName 列表
    private val fileNameMap: Map<String, String> = emptyMap(), // displayName → English filename
    private val bucket: String = "themes"                       // Supabase Storage bucket
) {
    /**
     * 根据 feeling displayName 返回对应的 URL（非默认主题）或 null（回退到 default）。
     * URL 格式: {STORAGE_URL}{bucket}/{englishName}.png
     */
    fun stickerUrl(feelingName: String): String? {
        if (isDefault) return null
        val fileName = (fileNameMap[feelingName] ?: feelingName) + ".png"
        return themeAssetUrl(bucket, fileName)
    }
}

object EmojiThemeManager {
    private const val PREFS_NAME = "emoji_theme"
    private const val KEY_THEME_ID = "theme_id"

    /** 内置默认主题 — 纯 Unicode emoji，无网络依赖 */
    val defaultTheme = EmojiThemeSet(
        id = "default",
        name = "默认表情",
        icon = "😊",
        isDefault = true,
        feelingNames = emptyList()
    )

    /** 从 manifest 构建的远端主题列表（含 default） */
    private var _themes: List<EmojiThemeSet> = listOf(defaultTheme)

    /** 所有可用主题（含 default + 远端） */
    val themes: List<EmojiThemeSet> get() = _themes

    private val _themeVersion = MutableStateFlow(0)
    val themeVersion: StateFlow<Int> = _themeVersion

    /**
     * 用 manifest 中的主题包更新主题列表。
     * 应在 App 启动并拉取 manifest 后调用。
     */
    fun updateThemes(packs: List<ThemePack>) {
        _themes = listOf(defaultTheme) + packs.map { pack ->
            EmojiThemeSet(
                id = pack.id,
                name = pack.name,
                icon = pack.icon?.let {
                    themeAssetUrl(pack.bucket, it)
                },
                isDefault = false,
                feelingNames = pack.feelings,
                fileNameMap = pack.fileNameMap,
                bucket = pack.bucket
            )
        }
        _themeVersion.value += 1
    }

    fun getCurrentTheme(context: Context): EmojiThemeSet {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_THEME_ID, defaultTheme.id) ?: defaultTheme.id
        return _themes.find { it.id == id } ?: defaultTheme
    }

    fun setCurrentTheme(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_ID, themeId).apply()
        _themeVersion.value += 1
    }

    /**
     * 获取当前主题下某个心情的展示内容。
     *
     * 规则：
     * 1. 当前主题覆盖此心情 → 返回贴纸 URL
     * 2. 否则 → 返回 Feeling 枚举的默认 emoji
     * 3. 枚举也没有 → 返回 "😶"
     */
    fun getEmoji(context: Context, feelingName: String): String {
        val theme = getCurrentTheme(context)
        return if (theme.feelingNames.contains(feelingName)) {
            theme.stickerUrl(feelingName) ?: fallbackEmoji(feelingName)
        } else {
            fallbackEmoji(feelingName)
        }
    }

    private fun fallbackEmoji(feelingName: String): String {
        return Feeling.fromDisplayName(feelingName)?.emoji ?: "😶"
    }
}

private fun themeAssetUrl(themeId: String, fileName: String): String {
    return "${SupabaseConfig.STORAGE_URL}themes/$themeId/$fileName"
}
