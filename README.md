# StandBy Us

StandBy Us 是一款面向两个人使用的 Android 状态共享 App。它不做传统账号体系，而是用本机生成的设备 ID 作为身份，两台设备通过 6 位配对码建立关系，然后共享彼此的状态、打卡、待办、相册和互动提醒。

项目当前是单模块 Android 工程，技术栈为 Kotlin、Jetpack Compose、Hilt、Room、OkHttp、Supabase REST API 和 Glance App Widget。

## 当前能力

| 模块 | 说明 |
| --- | --- |
| 首页 | 展示对方最新状态、自己的状态摘要、快捷互动入口，以及未读互动提示。 |
| 发布状态 | 选择心情或主题贴纸，填写 30 字以内补充文本，发布后写入状态时间线。 |
| 打卡 | 记录自己的打卡，展示双方今日、周、月和连续打卡数据，并可触发对方提醒。 |
| 待办 | 支持情侣清单、个人清单、任务新增、完成、删除，以及本地缓存兜底。 |
| 相册 | 从系统图片选择器上传照片，压缩后写入 Supabase Storage，并记录照片小记。 |
| 时光轴 | 合并展示自己和对方的状态历史，按更新时间倒序排列。 |
| 设置 | 生成/加入配对码，维护昵称、头像、主题包和解绑清理。 |
| 桌面小组件 | 读取 `widget_cache` 中的对方状态，显示心情、贴纸、活动和备注。 |
| 节日动效 | 按日期触发一次性庆祝覆盖层，支持生日、520、元旦、国庆、圣诞等配置。 |
| 伴侣通知 | 监听对方特定互动事件，获得通知权限后发送高优先级提醒。 |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 语言 | Kotlin 2.1.0 |
| UI | Jetpack Compose、Material3、Navigation Compose |
| 架构 | Single Activity、MVVM、Repository |
| 依赖注入 | Hilt |
| 本地数据 | Room 2.6.1、SharedPreferences |
| 远端数据 | Supabase REST API，手写 JSON 映射 |
| 网络 | OkHttp 4.12.0 |
| 图片 | Coil 2.7.0，支持远端图片和动图解码 |
| 小组件 | AndroidX Glance 1.1.1 |
| 测试 | JUnit4 |
| Android | minSdk 31，compileSdk/targetSdk 35，Java 17 |

## 导航结构

主导航由 `MainActivity` 中的 `NavHost` 驱动，底部导航只显示 5 个主页面。

| Route | 页面 | 入口 |
| --- | --- | --- |
| `home` | 首页 | 底部导航 |
| `checkin` | 打卡 | 底部导航 |
| `todo` | 待办 | 底部导航 |
| `album` | 相册 | 底部导航 |
| `history` | 时光轴 | 底部导航 |
| `post_status` | 发布状态 | 首页快捷入口 |
| `settings` | 设置 | 首页设置入口 |

## 项目结构

```text
app/src/main/java/com/standbyus/app/
|-- MainActivity.kt              # Activity 入口、NavHost、底部导航、通知权限、庆祝覆盖层
|-- StandByApplication.kt        # Hilt Application、设备 ID、主题清单、通知监听、小组件刷新
|-- data/
|   |-- remote/                  # SupabaseConfig、SupabaseService、ThemeRepository、StickerManager
|   |-- repository/              # 状态、配对、打卡、相册、待办、互动和解绑清理
|   |-- local/                   # Room 数据库、DAO、Entity 映射
|   `-- model/                   # UserStatus、PairingInfo、Feeling、ThemePack 等模型
|-- di/                          # Hilt module
|-- navigation/                  # 路由常量
|-- notification/                # 伴侣事件通知与通知门控
|-- ui/
|   |-- home/                    # 首页状态卡与互动
|   |-- poststatus/              # 发布状态、心情、贴纸和短句建议
|   |-- checkin/                 # 打卡页面与布局计算
|   |-- todo/                    # 待办清单
|   |-- album/                   # 相册上传和瀑布布局
|   |-- history/                 # 时间线
|   |-- settings/                # 配对、昵称、头像、主题选择
|   |-- celebration/             # 节日/生日覆盖层
|   |-- components/              # 复用 Compose 组件
|   `-- theme/                   # 颜色、字体、图标、主题包管理
`-- widget/                      # Glance 小组件
```

## 身份与配对

- 没有用户账号、密码或 Firebase Auth。
- `SupabaseService.getDeviceId()` 会在 `supabase_device` SharedPreferences 中生成并缓存 UUID。
- 配对码由 `PairingRepository` 生成，为 6 位大写字母/数字，字符集避开了容易混淆的 `0/O/1/I`。
- `pairs` 表保存 `pairId`、双方设备 ID 和双方昵称。
- 本地 `pairing` SharedPreferences 缓存配对状态、对方 ID、对方昵称、自己的昵称、头像和 pair id。
- 解绑时通过 `PairingCleanupRepository` 清理远端配对、状态、打卡、互动、相册、待办，以及本地 Room/Widget 缓存。

## 状态模型

当前状态发布已经从“中文心情名作为系统 ID”改为“稳定英文 key + 发布时主题快照”：

- `Feeling` 有 16 个稳定 key：`happy`、`missing`、`kiss`、`love`、`relaxed`、`hustling`、`thinking`、`watching`、`sad`、`upset`、`angry`、`anxious`、`tired`、`sick`、`bored`、`sleeping`。
- UI 显示为简体中文：开心、想你、亲亲、爱你、悠闲、奋斗、思考、追剧、难过、沮丧、生气、焦虑、疲惫、生病、无聊、睡觉。
- `UserStatus` 会保存发布当时的 `themeId`、`themeName`、`feelingKey`、`feelingLabel`、`feelingAsset`、`feelingFallbackEmoji`、`feelingColor`，以及可选贴纸快照。
- 旧字段 `feeling`、`feelingEmoji` 仍通过兼容逻辑读取，便于迁移旧数据。
- 发布页补充文本由 `PostStatusInputRules` 限制为 30 字，并在发布时去除首尾空白。

## 运行机制

```text
Compose Screen
  -> ViewModel
  -> Repository
  -> SupabaseService / Room DAO / SharedPreferences
```

- Supabase 使用原始 REST API 调用，不使用 Supabase Kotlin SDK，也不使用 Retrofit。
- 状态、打卡、互动都使用 `callbackFlow` + 6 秒轮询，没有 real-time subscription。
- `statuses` 是追加写入：每次发布都会插入一条新记录，首页只取对应用户最新一条，时光轴取历史列表。
- Room 是缓存层，不是完整离线优先数据源；远端成功返回时远端仍是事实来源。
- Room 数据库名为 `standbyus_db`，当前 version 为 6，启用了 `fallbackToDestructiveMigration()`。
- Widget 通过 `widget_cache` SharedPreferences 读取状态，不直接访问网络或 Room。
- App 启动时会初始化设备 ID、启动伴侣事件通知监听、加载远端主题清单，并刷新 Glance 小组件。

## 本地存储

| 存储 | 内容 |
| --- | --- |
| Room `status_cache` | 最新状态缓存 |
| Room `checkin_records` | 打卡记录缓存 |
| Room `todo_lists` | 待办清单缓存 |
| Room `todo_items` | 待办任务缓存 |
| SharedPreferences `supabase_device` | 本机设备 ID |
| SharedPreferences `pairing` | 配对状态、双方昵称、头像、pair id |
| SharedPreferences `widget_cache` | 桌面小组件显示数据 |
| SharedPreferences `emoji_theme` | 当前主题 ID |
| SharedPreferences `theme_manifest_cache` | 远端主题清单缓存 |
| SharedPreferences `celebration` | 每日庆祝动效展示记录 |
| SharedPreferences `notification_permission` | 通知权限是否已询问 |
| SharedPreferences `partner_event_notifications` | 通知去重记录 |

## Supabase 配置

项目把 Supabase 配置集中在：

```text
app/src/main/java/com/standbyus/app/data/remote/SupabaseConfig.kt
```

需要配置：

- `SUPABASE_URL`
- `ANON_KEY`
- 派生出的 `REST_URL`
- 派生出的 `STORAGE_URL`

注意：

- Android 客户端只能放 anon/public key，不能放 service role key。
- 当前项目没有 Supabase Auth；如果开启 RLS，需要为 anon 角色配置可用策略，否则客户端 REST 调用会失败。
- `docs/supabase/` 中已有部分增量 SQL：`todo-schema.sql`、`interactions.sql`、`status-snapshot-columns.sql`。

### 远端表

当前代码会访问这些表：

| 表 | 用途 |
| --- | --- |
| `statuses` | 状态发布与时光轴 |
| `pairs` | 6 位配对码与双方设备关系 |
| `checkins` | 打卡记录 |
| `interactions` | 首页快捷互动和打卡通知事件 |
| `photos` | 相册照片元数据 |
| `todo_lists` | 待办清单 |
| `todo_items` | 待办任务 |

基础表可按下面结构创建，再叠加 `docs/supabase/` 中的 SQL 文件：

```sql
create table if not exists public.statuses (
  id bigserial primary key,
  "userId" text not null default '',
  "doing" text not null default '',
  "customDoing" text not null default '',
  "themeId" text not null default 'default',
  "themeName" text not null default '默认表情',
  "feelingKey" text not null default 'happy',
  "feelingLabel" text not null default '开心',
  "feelingAsset" text not null default '😊',
  "feelingFallbackEmoji" text not null default '😊',
  "feelingColor" text not null default '#ffffd180',
  "stickerId" text,
  "stickerLabel" text,
  "stickerAsset" text,
  "note" text not null default '',
  "updatedAt" bigint not null default 0,
  "source" text not null default 'manual'
);

create index if not exists statuses_user_updated_idx
  on public.statuses ("userId", "updatedAt" desc);

create table if not exists public.pairs (
  id bigserial primary key,
  "pairId" text not null unique,
  "user1Id" text not null default '',
  "user2Id" text not null default '',
  "user1Name" text not null default '',
  "user2Name" text not null default ''
);

create table if not exists public.checkins (
  id bigserial primary key,
  "userId" text not null default '',
  "timestamp" bigint not null default 0,
  "note" text not null default ''
);

create index if not exists checkins_user_timestamp_idx
  on public.checkins ("userId", "timestamp" desc);

create table if not exists public.photos (
  id bigserial primary key,
  "deviceId" text not null default '',
  "url" text not null default '',
  "caption" text not null default '',
  "createdAt" bigint not null default 0
);

create index if not exists photos_device_created_idx
  on public.photos ("deviceId", "createdAt" desc);
```

随后执行：

```bash
docs/supabase/interactions.sql
docs/supabase/todo-schema.sql
docs/supabase/status-snapshot-columns.sql
```

### Storage buckets

| Bucket | 内容 |
| --- | --- |
| `our-story` | 相册上传照片，路径格式为 `{deviceId}/{timestamp}.jpg` |
| `themes` | 主题清单和主题资源，包含 `manifest.json`、`xiaoxin/`、`VV/`、`emoji-motion/` 等 |
| `stickers` | `StickerManager` 使用的贴纸上传 bucket |

`themes/manifest.json` 的本地参考文件在 `themes/manifest.json`。当前 manifest version 为 3，支持：

- `feelings`：按稳定心情 key 映射主题图片。
- `stickers`：不必固定为 16 个心情，可通过 tags 映射到心情或动作场景。
- 内置默认主题仍可使用 Unicode emoji 兜底。

## 构建与运行

准备环境：

- Android Studio
- JDK 17
- Android SDK 35
- 可访问 Google/MavenCentral 依赖仓库
- 可用的 Supabase 项目与所需表/bucket

Windows PowerShell：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

macOS/Linux：

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

调试 APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

安装到已连接设备：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 测试现状

当前 `app/src/test/java` 下有 37 个测试文件，覆盖模型序列化、主题包解析、主题快照、状态兼容、布局计算、首页互动顺序、通知去重门控、小组件颜色和输入规则等。

常用命令：

```bash
./gradlew testDebugUnitTest
```

如果只改 UI 布局计算、模型或 repository 边界逻辑，优先补充 JVM 单元测试。涉及 Compose 真机渲染、权限、通知和小组件行为时，还需要在模拟器或真机上手动验证。

## 开发约定

- UI 文案使用简体中文。
- 单 Activity + Compose Navigation；新增页面时同步维护 `Routes.kt` 和 `MainActivity.kt`。
- ViewModel 使用 Hilt 注入，公开 `StateFlow` 或 Compose state 给界面层。
- Repository 负责协调 Supabase、Room 和 SharedPreferences，不在 Compose 层直接操作远端。
- JSON 解析与序列化目前是手写 `toMap()` / `fromMap()`，没有 Moshi/Kotlinx Serialization。
- Supabase 表字段里有多处大小写敏感字段名，SQL 中需要保留双引号。
- 不要把 service role key、个人私密配置或生产库写入客户端代码。
- 本项目没有 Firebase SDK，也没有 Firebase 运行时依赖。

## 相关文档

- `AGENTS.md`：项目协作说明。
- `StandBy_Us_视觉规范文档.md`：视觉规范。
- `docs/design/`：UI 重设计记录。
- `docs/preview/`：静态预览页面。
- `docs/supabase/`：Supabase SQL 增量脚本。
- `docs/superpowers/specs/`：功能设计规格。
- `docs/superpowers/plans/`：阶段实现计划。
- `themes/`：主题包 manifest、本地素材和动态表情处理产物。
