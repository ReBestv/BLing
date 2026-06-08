# 主题包系统（贴纸主题）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 扩展现有 EmojiTheme 系统，支持基于 Supabase Storage 远端图片 URL 的贴纸主题包，通过 JSON manifest 动态注册主题，无需发版即可新增主题。

**Architecture:** ThemeRepository 在 App 启动时从 Supabase Storage 拉取 `themes/manifest.json` → 解析为 `ThemeManifest` → 缓存在 SharedPreferences。EmojiThemeManager 合并 manifest 主题 + 内置 default 主题，`getEmoji()` 按当前主题返回 URL（图片）或 Unicode emoji（兜底）。Settings UI 改为动态列表展示所有可用主题。

**Tech Stack:** Kotlin, OkHttp + org.json (与现有 SupabaseService 一致), SharedPreferences, Coil (已有), Hilt DI

---

## File Structure

```
app/src/main/java/com/standbyus/app/
├── data/
│   ├── model/
│   │   ├── Feeling.kt              [MODIFY] 新增 CRYING 枚举值
│   │   └── ThemePack.kt            [CREATE]  ThemeManifest + ThemePack 数据类 + JSON 解析
│   └── remote/
│       ├── ThemeRepository.kt      [CREATE]  拉取 manifest.json，缓存 SharedPreferences
│       └── SupabaseConfig.kt       [MODIFY]  新增 STORAGE_URL 常量
├── di/
│   └── AppModule.kt                [MODIFY]  提供 ThemeRepository
└── ui/
    ├── theme/
    │   └── EmojiTheme.kt           [REWRITE] EmojiThemeManager：合并 manifest + default，getEmoji() 返回 URL 或 emoji
    └── settings/
        ├── SettingsScreen.kt       [MODIFY]  硬编码 toggle → 动态主题列表
        └── SettingsViewModel.kt    [MODIFY]  异步加载 manifest，适配新 EmojiThemeManager
```

---

### Task 1: 新增 `CRYING` 枚举值到 Feeling.kt

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/data/model/Feeling.kt`

- [ ] **Step 1: 在 SLEEPING 之前添加 CRYING 枚举值**

```kotlin
// 在 SLEEPING 之前插入：
CRYING("哭哭", "😭",
    Color(0xFFBBDEFB), Color(0xFFBBDEFB), Color(0xFF90CAF9)),
```

编辑位置：第 42 行 `SLEEPING` 之前。插入后保持枚举尾部 `SLEEPING;` 不变。

完整 diff：
```
@@ -39,6 +39,8 @@
     BORED("无聊", "😑",
         Color(0xFFF0F4C3), Color(0xFFF0F4C3), Color(0xFFE6EE9C)),
+    CRYING("哭哭", "😭",
+        Color(0xFFBBDEFB), Color(0xFFBBDEFB), Color(0xFF90CAF9)),
     SLEEPING("睡觉", "😴",
         Color(0xFFE8EAF6), Color(0xFFE8EAF6), Color(0xFFC5CAE9));
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 2: 创建 ThemePack 数据模型

**Files:**
- Create: `app/src/main/java/com/standbyus/app/data/model/ThemePack.kt`

- [ ] **Step 1: 创建 ThemePack.kt**

```kotlin
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
 * @param id       主题唯一标识，即 Storage 中目录名
 * @param name     UI 展示名
 * @param icon     主题图标路径（相对于 bucket 根），null 表示无图标
 * @param feelings 该主题覆盖的心情 displayName 列表
 */
data class ThemePack(
    val id: String,
    val name: String,
    val icon: String?,
    val feelings: List<String>
) {
    companion object {
        fun fromJson(obj: JSONObject): ThemePack {
            val feelingsArray = obj.getJSONArray("feelings")
            val feelings = mutableListOf<String>()
            for (i in 0 until feelingsArray.length()) {
                feelings.add(feelingsArray.getString(i))
            }
            return ThemePack(
                id = obj.getString("id"),
                name = obj.getString("name"),
                icon = obj.optString("icon", null).takeIf { it.isNotEmpty() },
                feelings = feelings
            )
        }
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 3: 在 SupabaseConfig 中添加 STORAGE_URL

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/data/remote/SupabaseConfig.kt`

- [ ] **Step 1: 添加 STORAGE_URL 常量**

编辑 `SupabaseConfig.kt`，在 `REST_URL` 之后添加：

```kotlin
// Supabase Storage public URL — 用于加载公开 bucket 中的文件
const val STORAGE_URL = "$SUPABASE_URL/storage/v1/object/public/"
```

完整 diff：
```
@@ -16,3 +16,6 @@

     const val REST_URL = "$SUPABASE_URL/rest/v1/"
+
+    // Supabase Storage 公开 URL — 用于加载公开 bucket 中的文件
+    const val STORAGE_URL = "$SUPABASE_URL/storage/v1/object/public/"
 }
```

- [ ] **Step 2: 验证编译**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 4: 创建 ThemeRepository

**Files:**
- Create: `app/src/main/java/com/standbyus/app/data/remote/ThemeRepository.kt`

- [ ] **Step 1: 创建 ThemeRepository.kt**

```kotlin
package com.standbyus.app.data.remote

import android.content.Context
import android.util.Log
import com.standbyus.app.data.model.ThemeManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 负责从 Supabase Storage 拉取 themes/manifest.json，
 * 解析为 [ThemeManifest]，缓存到 SharedPreferences。
 */
@Singleton
class ThemeRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val manifestUrl =
        "${SupabaseConfig.STORAGE_URL}themes/manifest.json"

    companion object {
        private const val TAG = "ThemeRepository"
        private const val PREFS_NAME = "theme_manifest_cache"
        private const val KEY_MANIFEST_JSON = "manifest_json"
    }

    /**
     * 获取主题清单。优先从网络拉取，失败则用缓存兜底。
     * 返回 null 表示网络失败且无缓存（首次安装）。
     */
    suspend fun fetchManifest(context: Context): ThemeManifest? {
        return withContext(Dispatchers.IO) {
            try {
                val json = fetchFromNetwork()
                cacheManifest(context, json)
                ThemeManifest.fromJson(json)
            } catch (e: IOException) {
                Log.w(TAG, "Failed to fetch manifest, using cache", e)
                getCachedManifest(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse manifest", e)
                getCachedManifest(context)
            }
        }
    }

    private fun fetchFromNetwork(): String {
        val request = Request.Builder()
            .url(manifestUrl)
            .get()
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.message}")
        }
        return response.body?.string()
            ?: throw IOException("Empty response body")
    }

    private fun cacheManifest(context: Context, json: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_MANIFEST_JSON, json).apply()
    }

    private fun getCachedManifest(context: Context): ThemeManifest? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MANIFEST_JSON, null) ?: return null
        return try {
            ThemeManifest.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse cached manifest", e)
            null
        }
    }
}
```

- [ ] **Step 2: 在 AppModule 中提供 ThemeRepository**

编辑 `app/src/main/java/com/standbyus/app/di/AppModule.kt`，添加 `provideThemeRepository`：

```kotlin
@Provides
@Singleton
fun provideThemeRepository(): ThemeRepository = ThemeRepository()
```

完整 diff：
```
@@ -28,3 +28,7 @@
     @Provides
     fun provideCheckinDao(database: AppDatabase): CheckinDao = database.checkinDao()
+
+    @Provides
+    @Singleton
+    fun provideThemeRepository(): ThemeRepository = ThemeRepository()
 }
```

- [ ] **Step 3: 验证编译**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 5: 重写 EmojiThemeManager

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/ui/theme/EmojiTheme.kt`

- [ ] **Step 1: 重写 EmojiThemeManager**

将整个 `EmojiTheme.kt` 文件替换为以下内容：

```kotlin
package com.standbyus.app.ui.theme

import android.content.Context
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.ThemePack
import com.standbyus.app.data.remote.SupabaseConfig

/**
 * 单个主题的 UI 表示。合并了远端 ThemePack 和本地 emoji map。
 */
data class EmojiThemeSet(
    val id: String,
    val name: String,
    val icon: String?,          // 图标 URL 或 emoji（default 主题用 emoji）
    val isDefault: Boolean,     // true = 内置默认主题（纯 emoji，无图片）
    val feelingNames: List<String>  // 该主题覆盖的心情 displayName 列表
) {
    /**
     * 根据 feeling displayName 返回对应的 URL（非默认主题）或 null（回退到 default）。
     */
    fun stickerUrl(feelingName: String): String? {
        if (isDefault) return null
        return "${SupabaseConfig.STORAGE_URL}themes/$id/$feelingName.png"
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
                    "${SupabaseConfig.STORAGE_URL}themes/$it"
                },
                isDefault = false,
                feelingNames = pack.feelings
            )
        }
    }

    fun getCurrentTheme(context: Context): EmojiThemeSet {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_THEME_ID, defaultTheme.id) ?: defaultTheme.id
        return _themes.find { it.id == id } ?: defaultTheme
    }

    fun setCurrentTheme(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_ID, themeId).apply()
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
```

- [ ] **Step 2: 更新 SettingsViewModel 适配新 API**

编辑 `app/src/main/java/com/standbyus/app/ui/settings/SettingsViewModel.kt`：

添加导入：
```kotlin
import com.standbyus.app.data.remote.ThemeRepository
```

修改构造函数注入 `ThemeRepository`：
```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val supabaseService: SupabaseService,
    private val themeRepository: ThemeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
```

将 `availableThemes` 改为 `StateFlow`：
```kotlin
private val _availableThemes = MutableStateFlow(listOf(EmojiThemeManager.defaultTheme))
val availableThemes: StateFlow<List<EmojiThemeSet>> = _availableThemes.asStateFlow()
```

在 `init` 块末尾添加 manifest 加载：
```kotlin
init {
    checkExistingPair()
    _nicknameInput.value = prefs.getString("partner_nickname", "") ?: ""
    loadThemes()
}

private fun loadThemes() {
    viewModelScope.launch {
        val manifest = themeRepository.fetchManifest(context)
        if (manifest != null) {
            EmojiThemeManager.updateThemes(manifest.themes)
        }
        _availableThemes.value = EmojiThemeManager.themes
        // 同步当前主题状态
        _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
    }
}
```

完整的原 init 保持不变，在末尾(第 65 行 `}` 之后)插入 `loadThemes` 方法。移除旧的 `val availableThemes = EmojiThemeManager.themes` 行(第 43 行)。

完整 diff 关键部分：
```
@@ -19,6 +20,7 @@
 import com.standbyus.app.ui.theme.EmojiThemeSet
 import com.standbyus.app.ui.theme.EmojiThemeManager
+import com.standbyus.app.data.remote.ThemeRepository
 import javax.inject.Inject
 
@@ -22,4 +24,5 @@
 class SettingsViewModel @Inject constructor(
     private val pairingRepository: PairingRepository,
     private val supabaseService: SupabaseService,
+    private val themeRepository: ThemeRepository,
     @ApplicationContext private val context: Context
 ) : ViewModel() {
@@ -40,5 +43,6 @@
     private val _currentTheme = MutableStateFlow(EmojiThemeManager.getCurrentTheme(context))
     val currentTheme: StateFlow<EmojiThemeSet> = _currentTheme.asStateFlow()
 
-    val availableThemes = EmojiThemeManager.themes
+    private val _availableThemes = MutableStateFlow(listOf(EmojiThemeManager.defaultTheme))
+    val availableThemes: StateFlow<List<EmojiThemeSet>> = _availableThemes.asStateFlow()
 
@@ -62,3 +66,15 @@
         checkExistingPair()
         _nicknameInput.value = prefs.getString("partner_nickname", "") ?: ""
+        loadThemes()
     }
+
+    private fun loadThemes() {
+        viewModelScope.launch {
+            val manifest = themeRepository.fetchManifest(context)
+            if (manifest != null) {
+                EmojiThemeManager.updateThemes(manifest.themes)
+            }
+            _availableThemes.value = EmojiThemeManager.themes
+            _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
+        }
+    }
```

- [ ] **Step 3: 更新 SettingsScreen 为动态主题列表**

编辑 `app/src/main/java/com/standbyus/app/ui/settings/SettingsScreen.kt`。

需要额外导入：
```kotlin
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
```

替换外观 Section 中的猫咪主题 toggle（第 286-294 行）为动态主题列表：

```kotlin
// 动态主题列表
val themes by viewModel.availableThemes.collectAsState()

LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(0.dp)
) {
    items(themes, key = { it.id }) { theme ->
        ThemeRow(
            theme = theme,
            isSelected = currentTheme.id == theme.id,
            onClick = { viewModel.selectTheme(theme.id) }
        )
    }
}
```

在文件末尾（最后一个 `}` 之前）添加 `ThemeRow` 组件和 Coil 导入：

```kotlin
import coil.compose.AsyncImage
import coil.request.ImageRequest

// ===== Theme Row =====
@Composable
private fun ThemeRow(
    theme: EmojiThemeSet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 主题图标
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF5F0ED)),
            contentAlignment = Alignment.Center
        ) {
            if (theme.icon != null && theme.icon.startsWith("http")) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(theme.icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = theme.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                )
            } else {
                Text(
                    text = theme.icon ?: "🎨",
                    fontSize = 20.sp
                )
            }
        }

        // 主题名
        Text(
            text = theme.name,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // 选中勾
        if (isSelected) {
            Text(text = "✓", color = Primary, fontSize = 18.sp)
        }
    }

    HorizontalDivider(color = Border, thickness = 0.5.dp)
}
```

完整的 SettingsScreen diff（外观 Section 部分）：
```
// 在 Screen composable 函数中，添加：
+val themes by viewModel.availableThemes.collectAsState()

// 在 composable 顶部已有 import:
+import androidx.compose.foundation.lazy.LazyColumn
+import androidx.compose.foundation.lazy.items
+import androidx.compose.ui.platform.LocalContext
+import coil.compose.AsyncImage
+import coil.request.ImageRequest

// 替换外观 Section 中原来的 ToggleRow("😺 猫咪主题"...)
// 改为：
+LazyColumn(
+    modifier = Modifier.fillMaxWidth(),
+    verticalArrangement = Arrangement.spacedBy(0.dp)
+) {
+    items(themes, key = { it.id }) { theme ->
+        ThemeRow(
+            theme = theme,
+            isSelected = currentTheme.id == theme.id,
+            onClick = { viewModel.selectTheme(theme.id) }
+        )
+    }
+}
```

注意：深色模式 toggle 保留在主题列表上方，仍放在"外观" Section 中。

- [ ] **Step 4: 验证编译**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

---

### Task 6: 创建 manifest.json 并上传到 Supabase

**Files:**
- Create: `themes/manifest.json` (本地参考文件，再手动上传到 Supabase Storage)

- [ ] **Step 1: 创建本地 manifest.json**

```json
{
  "version": 1,
  "themes": [
    {
      "id": "小新",
      "name": "小新",
      "icon": "小新/开心.png",
      "feelings": [
        "开心", "难过", "疲惫", "生病", "悠闲", "想你", "睡觉",
        "奋斗", "思考", "爱你", "亲亲", "焦虑", "生气", "哭哭",
        "无聊", "追剧"
      ]
    }
  ]
}
```

将此内容写入 `E:\AndroidProject\StandByUs\themes\manifest.json`。

- [ ] **Step 2: 上传到 Supabase Storage**

使用 Supabase Dashboard 或手动操作：

1. 在 Supabase Storage 中创建 **公开 bucket** `themes`
2. 上传 `manifest.json` 到 bucket 根目录
3. 创建目录 `themes/小新/`
4. 将 `themes/小新/` 下的 16 张 PNG 上传到 `themes/小新/`

> 也可以在 Supabase SQL Editor 中执行以下 SQL 创建公开 bucket：
> ```sql
> INSERT INTO storage.buckets (id, name, public) VALUES ('themes', 'themes', true);
> ```

---

### Task 7: 清理旧代码

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/ui/theme/StandByUsTokens.kt`（如果存在）

- [ ] **Step 1: 检查并移除冗余的 EmojiTheme 枚举**

根据 explore agent 的分析，`StandByUsTokens.kt` 中存在一个未使用的 `EmojiTheme` 枚举（`DEFAULT`、`CATS`、`FOOD`）。如果确认该枚举未被任何代码引用，删除它。

```bash
# 检查是否有引用
grep -r "EmojiTheme\." --include="*.kt" app/src/main/
```

如果无引用，删除 `StandByUsTokens.kt` 中的 `EmojiTheme` 枚举定义。

- [ ] **Step 2: 全量编译验证**

```bash
./gradlew assembleDebug
```
Expected: BUILD SUCCESSFUL

---

## Execution Order

```
Task 1 (Feeling CRYING) ─┐
                          ├──→ Task 5 (EmojiThemeManager rewrite)
Task 2 (ThemePack模型)  ─┤        │
                          │        ├──→ Task 5.2 (SettingsViewModel)
Task 3 (STORAGE_URL)    ─┘        │        │
                                   │        └──→ Task 5.3 (SettingsScreen)
Task 4 (ThemeRepository) ─────────┘
                                         Task 6 (manifest.json + upload)
                                         Task 7 (cleanup)
```

Tasks 1-4 可并行执行。Task 5 依赖 1-4。Task 6 独立。Task 7 最后。
