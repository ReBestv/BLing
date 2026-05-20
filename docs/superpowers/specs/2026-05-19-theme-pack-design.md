# 主题包系统（贴纸主题）

## 概述

在现有 emoji 主题系统之上，支持**基于图片的贴纸主题包**。每个主题包是一组 PNG 表情图，通过 Supabase Storage 分发，运行时由 Coil 加载。文件名即心情名，自动映射到 Feeling 枚举 —— 无需手动配置映射关系。

与 2026-05-11-emoji-theme-design 的区别：此前仅支持 Unicode emoji 替换（字符串→字符串），本次扩展为支持**图片 URL**（字符串→URL+Coil AsyncImage）。

## 关键决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 文件名映射 | displayName = 文件名 | 零配置，约定优于配置 |
| 缺失枚举值 | 自动新增到 Feeling 枚举 | 一次添加，全局可用 |
| 图片存储 | Supabase Storage | 与现有 StickerManager 一致，不发版可更新 |
| 主题注册 | 远端 JSON 清单 | 新增主题无需改代码 |
| 默认兜底 | default 主题始终存在 | 纯 emoji，不走图片，离线时回退 |

## Supabase Storage 结构

```
bucket: themes/
├── manifest.json              ← 主题注册清单
├── 小新/                       ← themeId = 目录名
│   ├── 开心.png
│   ├── 难过.png
│   ├── 疲惫.png
│   ├── 生病.png
│   ├── 悠闲.png
│   ├── 想你.png
│   ├── 睡觉.png
│   ├── 奋斗.png
│   ├── 思考.png
│   ├── 爱你.png
│   ├── 亲亲.png
│   ├── 焦虑.png
│   ├── 生气.png
│   ├── 哭哭.png
│   ├── 无聊.png
│   └── 追剧.png
└── (未来更多主题包...)
```

URL 模式固定为：
```
https://<project>.supabase.co/storage/v1/object/public/themes/{themeId}/{feelingName}.png
```

## Manifest JSON 格式

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

**字段说明**：
- `id` — 主题唯一标识，即 Storage 中目录名，也是 URL 拼接的路径段
- `name` — UI 展示用的主题名（允许中文）
- `icon` — 主题图标路径（相对于 bucket 根目录），Settings 页面展示
- `feelings` — 该主题覆盖的心情 displayName 列表。**不在列表中的心情自动回退到 default 主题（Unicode emoji）**
- `default` 主题**不在 JSON 中声明**，由代码内置（`feelings` 为空，纯 emoji）

**加载策略**：
- App 启动时 GET `manifest.json`，成功则缓存到 SharedPreferences
- 缓存失效/加载失败时使用上次缓存的 manifest
- 首次安装无缓存时仅显示 default 主题

## 数据模型

### ThemeManifest（新增）

```kotlin
data class ThemeManifest(
    val version: Int,
    val themes: List<ThemePack>
)

data class ThemePack(
    val id: String,
    val name: String,
    val icon: String?,          // null 表示无图标，使用默认占位
    val feelings: List<String>  // 覆盖的心情列表
)
```

### EmojiThemeSet（修改现有）

```kotlin
// 现有字段保留，emojis Map 的值改为可为 URL
data class EmojiThemeSet(
    val id: String,
    val name: String,
    val icon: String?,
    val source: ThemeSource    // 新增：标记是本地 emoji 还是远端贴纸
)
```

### Feeling 枚举（修改现有）

新增 `CRYING` 枚举值以覆盖 `哭哭.png`：

```kotlin
CRYING("哭哭", "😭", Color(0xFFBBDEFB), Color(0xFFBBDEFB), Color(0xFF90CAF9))
```

> AGENTS.md 记载 9 个值但代码实际已有 16 个。本次新增 `CRYING` 后为 17 个。

## 代码改动

| 文件 | 改动类型 | 说明 |
|------|----------|------|
| `data/model/Feeling.kt` | 修改 | 新增 `CRYING` 枚举值 |
| `data/model/ThemePack.kt` | **新建** | `ThemeManifest` + `ThemePack` 数据类 |
| `data/remote/ThemeRepository.kt` | **新建** | 拉取 manifest.json、解析、缓存到 SharedPreferences |
| `ui/theme/EmojiTheme.kt` | 重写 | `EmojiThemeManager` 从 manifest 构建主题列表。`getEmoji()` 规则：当前主题的 feelings 包含该心情 → 返回 URL；否则 → 返回 `Feeling.emoji` |
| `di/SupabaseModule.kt` | 修改 | 注入 `ThemeRepository` |
| `ui/settings/SettingsScreen.kt` | 修改 | 主题选择器从硬编码 toggle 改为动态 LazyColumn（从 EmojiThemeManager.themes 读取） |
| `ui/settings/SettingsViewModel.kt` | 修改 | 适配新 EmojiThemeManager API |

**无需改动的文件**（已有 URL 支持）：
- `PostStatusScreen.kt` — `isEmoji()` + `AsyncImage` 已处理 URL
- `FeelingPicker.kt` — 同上
- `StatusCard.kt`、`HomeScreen.kt`、`HistoryScreen.kt`、`StandByWidget.kt` — 读取 `status.feelingEmoji`（发布时已解析为 URL 或 emoji 字符串）

## Fallback 链路

```
getEmoji(feelingName) 调用链:

1. 当前主题的 feelings 列表包含此心情？
   ├── YES → 返回 "{baseUrl}/{themeId}/{feelingName}.png" (URL)
   └── NO  → 2. 当前主题是 "default" 或有兜底？
              ├── YES → 返回 Feeling.fromDisplayName(name).emoji (Unicode)
              └── NO  → 返回 "😶" (终极兜底)
```

**渲染链**（已有的，无需改动）：
```
EmojiThemeManager.getEmoji() 返回值
├── startsWith("http") → Coil AsyncImage(model=url)
└── 否则              → Text(text=emoji)
```

## 主题选择 UI

Settings 页面主题区域改为动态列表：

```
┌──────────────────────────────┐
│  表情主题                     │
│  ┌────┬──────────────────┐   │
│  │ 😊 │ 默认        ✓    │   │
│  └────┴──────────────────┘   │
│  ┌────┬──────────────────┐   │
│  │ 🖼 │ 小新              │   │
│  └────┴──────────────────┘   │
│  ┌────┬──────────────────┐   │
│  │ 🖼 │ (未来更多主题...)  │   │
│  └────┴──────────────────┘   │
└──────────────────────────────┘
```

- 每个主题行显示：图标缩略图 + 主题名 + 选中勾
- 图标从 `theme.icon` URL 加载（Coil），加载失败显示默认占位符
- 选中状态存 SharedPreferences `emoji_theme`

## 上传工作流

新增一套主题包的步骤（无需改代码、无需发版）：

1. 准备 PNG 文件，按 Feeling.displayName 命名
2. 上传到 Supabase Storage：`themes/{themeId}/` 目录
3. 编辑 `manifest.json`，添加新主题条目
4. App 重启后自动拉取新 manifest → 主题可用

开发期可用 Supabase Dashboard 手动上传。未来可写一个上传脚本自动化。

## 缓存 & 网络策略

- **Manifest**：启动时拉取，SharedPreferences 缓存。加载失败用缓存兜底
- **贴纸图片**：Coil 默认磁盘缓存（LRU），自动管理。不额外处理
- **离线体验**：default 主题始终可用（纯 emoji，无网络依赖）。已缓存的贴纸图片离线可显示
