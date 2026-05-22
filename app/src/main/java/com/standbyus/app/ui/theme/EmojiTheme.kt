package com.standbyus.app.ui.theme

import android.content.Context
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.StatusFeelingSnapshot
import com.standbyus.app.data.model.ThemeFeeling
import com.standbyus.app.data.model.ThemePack
import com.standbyus.app.data.model.ThemeSticker
import com.standbyus.app.data.model.toHex
import com.standbyus.app.data.remote.SupabaseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class EmojiThemeSet(
    val id: String,
    val name: String,
    val bucket: String = "themes",
    val icon: String?,
    val feelings: List<ThemeFeeling>,
    val stickers: List<ThemeSticker> = emptyList(),
    val isDefault: Boolean = false
) {
    val resolvedIcon: String?
        get() = icon?.let { if (isDefault || it.startsWith("http")) it else themeAssetUrl(bucket, it) }

    fun feelingByKey(key: String): ThemeFeeling? = feelings.find { it.key == key }

    fun stickerById(id: String): ThemeSticker? = stickers.find { it.id == id }

    fun stickerUrl(feelingKey: String): String? {
        if (isDefault) return null
        val asset = feelingByKey(feelingKey)?.asset ?: return null
        return themeAssetUrl(bucket, asset)
    }

    fun themeStickerUrl(stickerId: String): String? {
        if (isDefault) return null
        val asset = stickerById(stickerId)?.asset ?: return null
        return themeAssetUrl(bucket, asset)
    }
}

object EmojiThemeManager {
    private const val PREFS_NAME = "emoji_theme"
    private const val KEY_THEME_ID = "theme_id"

    val defaultTheme = EmojiThemeSet(
        id = "default",
        name = "默认表情",
        bucket = "themes",
        icon = "😊",
        feelings = Feeling.entries.map { ThemeFeeling(it.key, it.displayName, it.emoji) },
        isDefault = true
    )

    private var _themes: List<EmojiThemeSet> = listOf(defaultTheme)
    val themes: List<EmojiThemeSet> get() = _themes

    private val _themeVersion = MutableStateFlow(0)
    val themeVersion: StateFlow<Int> = _themeVersion

    fun updateThemes(packs: List<ThemePack>) {
        _themes = listOf(defaultTheme) + packs.map { pack ->
            EmojiThemeSet(
                id = pack.id,
                name = pack.name,
                bucket = pack.bucket,
                icon = pack.icon,
                feelings = pack.feelings,
                stickers = pack.stickers
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

    fun getEmoji(context: Context, feelingKey: String?): String {
        return createSnapshot(getCurrentTheme(context), feelingKey).feelingAsset
    }

    fun labelFor(context: Context, feelingKey: String?): String {
        return createSnapshot(getCurrentTheme(context), feelingKey).feelingLabel
    }

    fun createSnapshot(
        theme: EmojiThemeSet,
        feelingKey: String?,
        stickerId: String? = null
    ): StatusFeelingSnapshot {
        val definition = feelingKey?.let { Feeling.fromKey(it) }
        val themeFeeling = definition?.let { theme.feelingByKey(it.key) }
        val selectedSticker = stickerId?.let { theme.stickerById(it) }
        val selectedStickerAsset = selectedSticker?.let { sticker ->
            when {
                sticker.asset.startsWith("http://") -> sticker.asset
                sticker.asset.startsWith("https://") -> sticker.asset
                else -> themeAssetUrl(theme.bucket, sticker.asset)
            }
        }
        val asset = when {
            selectedStickerAsset != null -> selectedStickerAsset
            definition == null -> ""
            theme.isDefault -> definition.emoji
            themeFeeling?.asset?.startsWith("http://") == true -> themeFeeling.asset
            themeFeeling?.asset?.startsWith("https://") == true -> themeFeeling.asset
            !themeFeeling?.asset.isNullOrBlank() -> themeAssetUrl(theme.bucket, themeFeeling.asset!!)
            else -> definition.emoji
        }

        return StatusFeelingSnapshot(
            themeId = theme.id,
            themeName = theme.name,
            feelingKey = definition?.key.orEmpty(),
            feelingLabel = definition?.let { themeFeeling?.label ?: it.displayName }.orEmpty(),
            feelingAsset = asset,
            feelingFallbackEmoji = definition?.emoji.orEmpty(),
            feelingColor = definition?.color?.toHex().orEmpty(),
            stickerId = selectedSticker?.id,
            stickerLabel = selectedSticker?.label,
            stickerAsset = selectedStickerAsset
        )
    }
}

fun themeAssetUrl(bucket: String, fileName: String): String {
    return "${SupabaseConfig.STORAGE_URL}themes/$bucket/$fileName"
}
