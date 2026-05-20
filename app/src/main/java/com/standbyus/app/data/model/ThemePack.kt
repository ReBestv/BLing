package com.standbyus.app.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * 远端主题注册清单，从 themes/manifest.json 解析。
 */
data class ThemeManifest(
    val version: Int,
    val themes: List<ThemePack>
) {
    companion object {
        fun fromJson(json: String): ThemeManifest {
            val root = JSONObject(json)
            val version = root.getInt("version")
            val themesArray = root.getJSONArray("themes")
            val themes = mutableListOf<ThemePack>()
            for (i in 0 until themesArray.length()) {
                themes.add(ThemePack.fromJson(themesArray.getJSONObject(i)))
            }
            return ThemeManifest(version, themes)
        }
    }
}

data class ThemeFeeling(
    val key: String,
    val label: String,
    val asset: String? = null
)

/**
 * 单个主题包定义。
 * @param id          主题唯一标识
 * @param name        UI 展示名
 * @param bucket      Supabase Storage bucket 名称（主题图片所在 bucket）
 * @param icon        主题图标文件名（相对于 bucket 根），null 表示无图标
 * @param feelings    该主题覆盖的心情 key/label/asset 列表
 */
data class ThemePack(
    val id: String,
    val name: String,
    val bucket: String,
    val icon: String?,
    val feelings: List<ThemeFeeling>
) {
    fun feelingByKey(key: String): ThemeFeeling? = feelings.find { it.key == key }

    fun fileName(feelingKey: String): String = feelingByKey(feelingKey)?.asset ?: "$feelingKey.png"

    companion object {
        fun fromJson(obj: JSONObject): ThemePack {
            val feelingsArray = obj.getJSONArray("feelings")
            val feelings = parseFeelings(feelingsArray, obj.optJSONObject("fileNameMap"))
            return ThemePack(
                id = obj.getString("id"),
                name = obj.getString("name"),
                bucket = obj.optString("bucket", "themes"),
                icon = obj.optString("icon", "").takeIf { it.isNotEmpty() },
                feelings = feelings
            )
        }

        private fun parseFeelings(
            feelingsArray: JSONArray,
            legacyFileNameMap: JSONObject?
        ): List<ThemeFeeling> {
            return (0 until feelingsArray.length()).map { index ->
                val raw = feelingsArray.get(index)
                if (raw is JSONObject) {
                    ThemeFeeling(
                        key = raw.getString("key"),
                        label = raw.getString("label"),
                        asset = raw.optString("asset", "").takeIf { it.isNotEmpty() }
                    )
                } else {
                    val legacyLabel = raw.toString()
                    val feeling = Feeling.fromLegacyLabel(legacyLabel) ?: Feeling.HAPPY
                    val mappedName = legacyFileNameMap?.optString(legacyLabel, "").orEmpty()
                    ThemeFeeling(
                        key = feeling.key,
                        label = legacyLabel,
                        asset = mappedName.takeIf { it.isNotEmpty() }?.let { "$it.png" } ?: "${feeling.key}.png"
                    )
                }
            }
        }
    }
}
