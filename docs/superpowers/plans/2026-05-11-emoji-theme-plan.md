# 表情主题系统 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 每台设备可独立选择表情主题，发布状态时用当前主题的 emoji，对方原样展示。

**Architecture:** `EmojiTheme` 数据类 + `EmojiThemeManager`（SharedPreferences 存储当前主题）+ FeelingPicker 从主题取表情。新增 3 个文件，修改 3 个文件。

---

### Task 1: 创建 EmojiTheme 数据类和管理器

**Files:**
- Create: `app/src/main/java/com/standbyus/app/ui/theme/EmojiTheme.kt`
- Modify: `PostStatusViewModel.kt` 使用主题取 emoji

- [ ] **Step 1: 创建 EmojiTheme.kt**

```kotlin
package com.standbyus.app.ui.theme

import android.content.Context
import android.content.SharedPreferences

data class EmojiTheme(
    val id: String,
    val name: String,
    val icon: String,
    val emojis: Map<String, String>
)

object EmojiThemeManager {
    private const val PREFS_NAME = "emoji_theme"
    private const val KEY_THEME_ID = "theme_id"

    val themes = listOf(
        EmojiTheme("default", "经典", "😊", mapOf(
            "开心" to "😊", "难过" to "😢", "疲惫" to "😫",
            "生病" to "🤒", "悠闲" to "😌", "想你了" to "🥰",
            "睡觉" to "😴", "奋斗" to "💪", "思考" to "🤔"
        )),
        EmojiTheme("cat", "小猫", "🐱", mapOf(
            "开心" to "😺", "难过" to "😿", "疲惫" to "🙀",
            "生病" to "😾", "悠闲" to "😸", "想你了" to "😽",
            "睡觉" to "😴", "奋斗" to "😼", "思考" to "🤔"
        ))
    )

    private val defaultTheme: EmojiTheme get() = themes.first()

    fun getCurrentTheme(context: Context): EmojiTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_THEME_ID, defaultTheme.id) ?: defaultTheme.id
        return themes.find { it.id == id } ?: defaultTheme
    }

    fun setCurrentTheme(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_THEME_ID, themeId).apply()
    }

    /** 根据心情名获取当前主题的表情 */
    fun getEmoji(context: Context, feelingName: String): String {
        val theme = getCurrentTheme(context)
        return theme.emojis[feelingName] ?: defaultTheme.emojis[feelingName] ?: "😊"
    }
}
```

- [ ] **Step 2: 修改 PostStatusViewModel 用主题取 emoji**

添加 `@ApplicationContext` 注入，`publish()` 中 `feelingEmoji` 从 `EmojiThemeManager.getEmoji(context, selectedFeeling.displayName)` 取。

```kotlin
@HiltViewModel
class PostStatusViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // ... existing code ...

    fun publish() {
        viewModelScope.launch {
            // ... existing code ...
            val status = UserStatus(
                userId = userId,
                doing = customDoing.ifEmpty { "发呆" },
                customDoing = customDoing,
                feeling = selectedFeeling.displayName,
                feelingColor = selectedFeeling.color.toString(),
                feelingEmoji = EmojiThemeManager.getEmoji(context, selectedFeeling.displayName),
                note = ""
            )
            // ... rest unchanged ...
        }
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd "E:/AndroidProject/StandByUs" && ./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

---

### Task 2: FeelingPicker 从主题取表情

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/ui/components/FeelingPicker.kt`

**Context:** FeelingPicker 目前写死 `feeling.emoji`，改为从当前主题取。

- [ ] **Step 1: 修改 FeelingPicker**

添加 `@ApplicationContext` 参数（通过 Composable 参数传入），将 `feeling.emoji` 替换为 `EmojiThemeManager.getEmoji(context, feeling.displayName)`。

```kotlin
@Composable
fun FeelingPicker(
    feelings: List<Feeling> = Feeling.entries,
    selectedFeeling: Feeling,
    onFeelingSelected: (Feeling) -> Unit,
    emojiGetter: (Feeling) -> String = { it.emoji },
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(feelings) { feeling ->
            val isSelected = feeling == selectedFeeling
            val emoji = emojiGetter(feeling)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onFeelingSelected(feeling) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) feeling.color
                            else feeling.color.copy(alpha = 0.2f)
                        )
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = feeling.color,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 26.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feeling.displayName,
                    fontSize = 11.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

- [ ] **Step 2: 修改 PostStatusScreen 传入 emojiGetter**

```kotlin
// 在 PostStatusScreen 中
import com.standbyus.app.ui.theme.EmojiThemeManager

// 获取 Context
val context = LocalContext.current

FeelingPicker(
    selectedFeeling = viewModel.selectedFeeling,
    onFeelingSelected = { viewModel.selectFeeling(it) },
    emojiGetter = { feeling ->
        EmojiThemeManager.getEmoji(context, feeling.displayName)
    },
    modifier = Modifier.weight(1f)
)
```

- [ ] **Step 3: 编译验证**

Run: `cd "E:/AndroidProject/StandByUs" && ./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

---

### Task 3: 设置页添加主题选择器

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/standbyus/app/ui/settings/SettingsViewModel.kt`

- [ ] **Step 1: SettingsViewModel 添加主题状态**

```kotlin
import com.standbyus.app.ui.theme.EmojiThemeManager
import com.standbyus.app.ui.theme.EmojiTheme

// 添加状态
private val _currentTheme = MutableStateFlow(EmojiThemeManager.getCurrentTheme(context))
val currentTheme: StateFlow<EmojiTheme> = _currentTheme.asStateFlow()

val availableThemes = EmojiThemeManager.themes

fun selectTheme(themeId: String) {
    EmojiThemeManager.setCurrentTheme(context, themeId)
    _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
}
```

- [ ] **Step 2: SettingsScreen 添加主题卡片**

在已绑定卡片下方，添加主题选择行：

```kotlin
// 在已绑定 card 下方
if (isPaired) {
    // 现有 "已绑定" card...
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // 主题选择
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("表情主题", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.availableThemes.forEach { theme ->
                    val isActive = viewModel.currentTheme.value.id == theme.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectTheme(theme.id) }
                            .background(
                                if (isActive) Orange.copy(alpha = 0.1f)
                                else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(text = theme.icon, fontSize = 28.sp)
                        Text(
                            text = theme.name,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) Orange else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd "E:/AndroidProject/StandByUs" && ./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL
