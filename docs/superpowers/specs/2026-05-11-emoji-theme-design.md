# 表情主题系统

## 概述

每个设备可独立选择表情主题。发布状态时，`feelingEmoji` 使用当前主题对应的表情；对方收到后原样展示。各设备互不影响。

## 数据模型

```kotlin
data class EmojiTheme(
    val id: String,          // 唯一标识
    val name: String,        // 显示名
    val icon: String,        // 主题图标
    val emojis: Map<String, String>  // 心情displayName → 表情
)
```

## 预设主题

| ID | 名称 | 图标 | 示例 |
|----|------|------|------|
| `default` | 经典 | 😊 | 😊😢😫🤒😌🥰😴💪🤔 |
| `cat` | 小猫 | 🐱 | 😺😿🙀😾😸😽😴😼🤔 |

扩展新主题只需在 `EmojiThemeConfig` 中加一个 `EmojiTheme` 实例。

## 存储

- 当前主题 ID 存 `SharedPreferences(key: "emoji_theme", default: "default")`
- 各设备独立设置

## 影响范围

| 文件 | 改动 |
|------|------|
| `ui/theme/EmojiTheme.kt` | **新建** — 数据类 + 预设主题列表 |
| `ui/theme/EmojiThemeConfig.kt` | **新建** — 当前主题管理 |
| `ui/components/FeelingPicker.kt` | 表情从当前主题取，非 Feeling 枚举 |
| `ui/settings/SettingsScreen.kt` | 添加主题选择器行 |
| `ui/settings/SettingsViewModel.kt` | 主题切换逻辑 |
| `PostStatusViewModel.kt` | `feelingEmoji` 从当前主题取 |
| 所有展示 emoji 的组件 | `status.feelingEmoji` 直接展示，无需改动 |
