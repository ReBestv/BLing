# StandBy Us

**情侣状态分享 App** — 两人之间的实时状态共享，支持心情、活动记录和桌面小组件。

---

## 项目简介

StandBy Us 是一个 Android 私密状态共享应用，专为两人使用设计。你可以实时分享当前的心情和正在做的事，对方的更新也会即时呈现在你的首页上。

- 灵感来源：[StandBy](https://standby.vip/#/PC)
- 技术栈：Kotlin + Jetpack Compose + Supabase

---

## 技术栈

| 层级 | 技术 | 用途 |
|------|------|------|
| UI | Jetpack Compose + Material3 | 声明式 UI |
| 架构 | MVVM (ViewModel + Repository) | 业务逻辑分层 |
| DI | Hilt | 依赖注入 |
| 本地存储 | Room | 状态缓存 |
| 后端 | Supabase REST API | 数据同步 |
| 网络 | OkHttp | HTTP 客户端 |
| 小组件 | Glance | 桌面 Widget |
| 最低 SDK | Android 12 (API 31) | |
| 目标 SDK | Android 15 (API 35) | |

---

## 架构概览

```
app/
├── MainActivity.kt              # 入口 Activity + 庆祝动画集成
├── StandByApplication.kt        # Hilt 入口 + 设备 ID 初始化
├── data/
│   ├── local/                   # Room 本地缓存
│   │   ├── AppDatabase.kt
│   │   ├── StatusDao.kt
│   │   └── StatusEntity.kt
│   ├── model/                   # 数据模型
│   │   ├── Doing.kt             # 活动预设枚举（12 种）
│   │   ├── Feeling.kt           # 心情预设枚举（9 种 + 颜色）
│   │   ├── PairingInfo.kt       # 配对信息
│   │   └── UserStatus.kt        # 用户状态实体
│   ├── remote/                  # 网络层
│   │   ├── SupabaseConfig.kt    # Supabase 配置
│   │   ├── SupabaseService.kt   # Supabase REST API 客户端
│   │   ├── ThemeRepository.kt   # 主题包清单获取
│   │   └── StickerManager.kt    # 贴纸缓存
│   └── repository/              # 数据仓库
│       ├── PairingRepository.kt # 配对业务逻辑
│       └── StatusRepository.kt  # 状态业务逻辑
├── di/
│   └── AppModule.kt             # Room 数据库与主题仓库提供
├── navigation/
│   └── Routes.kt                # 导航路由定义
├── ui/
│   ├── celebration/             # 庆祝动画
│   │   ├── CelebrationDay.kt    # 纪念日数据类
│   │   ├── CelebrationConfig.kt # 纪念日配置列表
│   │   └── CelebrationOverlay.kt# 全屏粒子动画覆盖层
│   ├── components/              # 可复用组件
│   │   ├── AvatarWithGlow.kt    # 呼吸发光头像
│   │   ├── DoingPicker.kt       # 活动选择器
│   │   ├── FeelingPicker.kt     # 心情选择器（含颜色动画）
│   │   └── StatusCard.kt        # 状态卡片（渐变背景）
│   ├── home/                    # 首页
│   │   ├── HomeScreen.kt        # 对方卡片 + 我的状态 + 发布按钮
│   │   └── HomeViewModel.kt     # 状态轮询 + 伙伴查找
│   ├── history/                 # 历史记录
│   │   ├── HistoryScreen.kt     # 时间线列表
│   │   └── HistoryViewModel.kt  # 历史查询
│   ├── poststatus/              # 发布状态
│   │   ├── PostStatusScreen.kt  # 心情/活动选择 + 备注
│   │   └── PostStatusViewModel.kt
│   ├── settings/                # 设置/配对
│   │   ├── SettingsScreen.kt    # 配对 + 已绑定状态
│   │   └── SettingsViewModel.kt
│   └── theme/                   # 主题
│       ├── Color.kt             # 品牌色
│       ├── EmotionColors.kt     # 心情颜色映射
│       ├── Theme.kt             # Material3 主题配置
│       └── Type.kt              # 字体配置
└── widget/                      # 桌面小组件
    ├── StandByWidget.kt         # 4×4 Glance 组件
    └── StandByWidgetReceiver.kt
```

---

## 核心功能

### 状态共享
- **Doing（正在做）**：12 种活动预设 + 自定义输入
- **Feeling（心情）**：16 种心情，每种配有 emoji + 颜色 + 渐变
- **备注**：可选的补充文字
- **实时更新**：通过轮询（6 秒间隔）获取对方状态

### 配对系统
- **生成配对码**：随机 6 位字母数字码
- **加入配对**：输入对方配对码完成绑定
- **持久化**：配对信息存储于 Supabase + 本地缓存，重启无需重连

### 庆祝动画
- **纪念日检测**：App 启动时匹配配置日期
- **全屏粒子动画**：Canvas 绘制彩色飘落圆点
- **弹性缩放**：中心 emoji + 祝福语弹簧弹入
- **每日一次**：SharedPreferences 控制显示频率

### 桌面 Widget
- 4×4 Glance 组件，显示对方最新状态（emoji + 心情 + 活动）
- 数据通过 SharedPreferences 缓存更新

---

## 后端（Supabase）

### 数据表

```sql
CREATE TABLE statuses (
  id BIGSERIAL PRIMARY KEY,
  "userId" TEXT NOT NULL DEFAULT '',
  "doing" TEXT NOT NULL DEFAULT '',
  "customDoing" TEXT NOT NULL DEFAULT '',
  "feeling" TEXT NOT NULL DEFAULT '开心',
  "feelingColor" TEXT NOT NULL DEFAULT '#FFD93D',
  "feelingEmoji" TEXT NOT NULL DEFAULT '😊',
  "note" TEXT NOT NULL DEFAULT '',
  "updatedAt" BIGINT NOT NULL DEFAULT 0,
  "source" TEXT NOT NULL DEFAULT 'manual'
);

CREATE TABLE pairs (
  id BIGSERIAL PRIMARY KEY,
  "pairId" TEXT NOT NULL UNIQUE,
  "user1Id" TEXT NOT NULL DEFAULT '',
  "user2Id" TEXT NOT NULL DEFAULT ''
);
```

### API 配置

在 `SupabaseConfig.kt` 中配置：
```kotlin
const val SUPABASE_URL = "https://你的项目.supabase.co"
const val ANON_KEY = "你的anon-public-key"
```

---

## 开发记录

### 2026-05-07
- 项目初始化，从 Firebase 迁移至 Bmob，再迁移至 Supabase
- 实现完整状态共享功能（发布心情、活动、备注）
- 实现配对系统（生成/加入配对码）
- 实现主页轮询展示对方状态
- 实现发布状态和配对庆祝动画
- 实现纪念日全屏庆祝动画
- 实现桌面小组件
- 解决 Android 模拟器 DNS 问题：本地缓存配对状态和伙伴 ID，避免网络不通时卡死

---

## 构建与运行

```bash
# 调试构建
cd StandByUs
./gradlew assembleDebug

# 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

首次运行前需：
1. 在 [Supabase](https://supabase.com) 创建项目并执行上方 SQL 建表
2. 将项目 URL 和 anon key 填入 `SupabaseConfig.kt`
3. 在 Supabase 控制台关闭 RLS（或配置开放策略）
