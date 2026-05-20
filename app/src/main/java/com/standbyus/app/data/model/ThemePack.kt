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

/**
 * 单个主题包定义。
 * @param id          主题唯一标识
 * @param name        UI 展示名
 * @param bucket      Supabase Storage bucket 名称（主题图片所在 bucket）
 * @param icon        主题图标文件名（相对于 bucket 根），null 表示无图标
 * @param feelings    该主题覆盖的心情 displayName 列表
 * @param fileNameMap displayName → 英文文件名的映射（不含 .png 后缀）。
 *                    未在 map 中的心情默认用 "{displayName}.png"
 */
data class ThemePack(
    val id: String,
    val name: String,
    val bucket: String,
    val icon: String?,
    val feelings: List<String>,
    val fileNameMap: Map<String, String> = emptyMap()
) {
    /** 根据 displayName 获取 Storage 上的文件名（含 .png 扩展名） */
    fun fileName(feelingName: String): String {
        return (fileNameMap[feelingName] ?: feelingName) + ".png"
    }

    companion object {
        fun fromJson(obj: JSONObject): ThemePack {
            val feelingsArray = obj.getJSONArray("feelings")
            val feelings = mutableListOf<String>()
            for (i in 0 until feelingsArray.length()) {
                feelings.add(feelingsArray.getString(i))
            }
            val fileNameMap = mutableMapOf<String, String>()
            if (obj.has("fileNameMap")) {
                val mapObj = obj.getJSONObject("fileNameMap")
                for (key in mapObj.keys()) {
                    fileNameMap[key] = mapObj.getString(key)
                }
            }
            return ThemePack(
                id = obj.getString("id"),
                name = obj.getString("name"),
                bucket = obj.optString("bucket", "themes"),
                icon = obj.optString("icon", "").takeIf { it.isNotEmpty() },
                feelings = feelings,
                fileNameMap = fileNameMap
            )
        }
    }
}
