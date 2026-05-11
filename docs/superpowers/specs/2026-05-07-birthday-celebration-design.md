# 生日/纪念日全屏庆祝动画

## 概述

在 App 启动时检测当前日期，若匹配到预配置的节日或纪念日，则弹出全屏庆祝动画覆盖层。配置方式为简单的数据类列表，用户可自由增删。

## 配置

### CelebrationDay 数据类

```kotlin
data class CelebrationDay(
    val month: Int,
    val day: Int,
    val emoji: String,
    val message: String
)
```

### CelebrationConfig

```kotlin
object CelebrationConfig {
    val days = listOf(
        CelebrationDay(5, 7, "🎂", "生日快乐！"),
        CelebrationDay(12, 25, "🎄", "圣诞快乐！"),
        CelebrationDay(2, 14, "💕", "情人节快乐！"),
    )
}
```

## 触发逻辑

1. App 启动（MainActivity.onCreate 或 HomeScreen 首次组合）
2. 获取当前日期（年/月/日）
3. 查询 CelebrationConfig 中是否有匹配的 month + day
4. 检查 SharedPreferences 中今日是否已显示过（key: "celebration_shown_2026-05-07"）
5. 若匹配且未显示 → 弹出全屏动画
6. 用户点击任意位置关闭，记录今日已显示

## 动画设计

### 全屏覆盖层（CelebrationOverlay）

| 元素 | 实现 |
|------|------|
| 遮罩 | 全屏半透明黑色（alpha 0.4） |
| 粒子 | Canvas 绘制的彩色圆点 + 随机 emoji（🎉🎊🎈🎂🎁） |
| 粒子颜色 | App 情感色板（#FF9F43 橙, #FFD93D 黄, 等） |
| 粒子数量 | 15-20 个粒子 |
| 中心内容 | 大号 emoji（48sp）+ 祝福语（24sp, bold） |
| 入场 | 弹性缩放（spring 效果） |
| 粒子运动 | infiniteRepeatable + animateFloat，从上往下飘落 |
| 关闭 | 点击任意位置 或 4 秒后自动消失 |
| 每日频率 | 每天只弹出一次 |

### 实现文件

- `ui/celebration/CelebrationDay.kt` — 数据类
- `ui/celebration/CelebrationConfig.kt` — 配置
- `ui/celebration/CelebrationOverlay.kt` — 全屏动画覆盖层 Composable
- `MainActivity.kt` — 修改，在 HomeScreen 层之上添加覆盖层

## 风格一致性

- 颜色取自 App 现有情感色板
- 圆角/字体与 Material3 主题一致
- 粒子使用 App 的 brand color + emotion colors
- 无额外依赖，纯 Compose Animation + Canvas

## 文件结构

```
ui/celebration/
├── CelebrationDay.kt       # 数据类
├── CelebrationConfig.kt    # 配置列表
└── CelebrationOverlay.kt   # 全屏动画 Composable
```
