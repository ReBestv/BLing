# StandBy Us

StandBy Us 是一款给情侣或亲密伙伴使用的 Android 状态共享 App。它不使用账号体系，而是用本机生成的设备 ID 作为身份，两台设备通过 6 位配对码建立关系后，可以共享状态、打卡、待办、相册和轻互动提醒。

项目当前是单模块 Android 工程，技术栈为 Kotlin、Jetpack Compose、Hilt、Room、OkHttp、Supabase REST API 和 Glance App Widget。

## 功能概览

| 模块 | 说明 |
| --- | --- |
| 首页 | 展示对方最新状态、自己的状态摘要、快捷互动入口和未读互动提示 |
| 发布状态 | 选择心情或主题贴纸，填写 30 字以内补充文本，发布后写入状态时间线 |
| 打卡 | 记录自己的打卡，展示双方今日、周、月和连续打卡数据，并可触发对方提醒 |
| 待办 | 支持情侣清单、个人清单、任务新增、完成、删除，以及本地缓存兜底 |
| 相册 | 从系统图片选择器上传照片，压缩后写入 Supabase Storage，并记录照片小记 |
| 时光轴 | 合并展示自己和对方的状态历史，按更新时间倒序排列 |
| 设置 | 生成/加入配对码，维护昵称、头像、主题包和解绑清理 |
| 桌面小组件 | 读取 `widget_cache` 中的对方状态，显示心情、贴纸、活动和备注 |
| 节日动效 | 按日期触发一次性庆祝覆盖层，配置集中在 `CelebrationConfig` |
| 伴侣通知 | 监听对方特定互动事件，获得通知权限后发送高优先级提醒 |

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

## 快速开始

准备环境：

- Android Studio
- JDK 17
- Android SDK 35
- 可访问 Google/MavenCentral 依赖仓库
- 一个可用的 Supabase 项目

复制本地配置示例：

```powershell
copy local.properties.example local.properties
```

在 `local.properties` 中填入你的 Supabase 配置：

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-or-publishable-key
```

注意：

- `local.properties` 已被 `.gitignore` 忽略，不要提交。
- Android 客户端只能使用 anon/public/publishable key，不要使用 service role key。
- 如果开启 Supabase RLS，需要为 anon 角色配置可用策略，否则客户端 REST 调用会失败。

构建和测试：

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

## Supabase 准备

项目不使用 Firebase，也不使用 Supabase SDK。所有远端访问都通过 `SupabaseService` 使用 OkHttp 调用 Supabase REST API。

代码会访问这些表：

| 表 | 用途 |
| --- | --- |
| `statuses` | 状态发布与时光轴 |
| `pairs` | 6 位配对码与双方设备关系 |
| `checkins` | 打卡记录 |
| `interactions` | 首页快捷互动和打卡通知事件 |
| `photos` | 相册照片元数据 |
| `todo_lists` | 待办清单 |
| `todo_items` | 待办任务 |

需要的 Storage buckets：

| Bucket | 内容 |
| --- | --- |
| `our-story` | 相册上传照片，路径格式为 `{deviceId}/{timestamp}.jpg` |
| `themes` | 主题 manifest 和主题资源 |
| `stickers` | `StickerManager` 使用的贴纸上传 bucket |

`docs/supabase/` 中包含部分增量 SQL：

```text
docs/supabase/interactions.sql
docs/supabase/todo-schema.sql
docs/supabase/status-snapshot-columns.sql
```

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
|-- ui/                          # Compose 页面、布局、组件和主题
`-- widget/                      # Glance 小组件
```

主导航由 `MainActivity` 中的 `NavHost` 驱动，主要路由包括 `home`、`checkin`、`todo`、`album`、`history`、`post_status` 和 `settings`。

## 开发约定

- UI 文案使用简体中文。
- 设备身份来自 `supabase_device` SharedPreferences 中的 UUID。
- Repository 负责协调 Supabase、Room 和 SharedPreferences，不在 Compose 层直接操作远端。
- Supabase 表字段里有多处大小写敏感字段名，SQL 中需要保留双引号。
- 状态、打卡、互动同步使用 6 秒轮询，没有 real-time subscription。
- Room 当前启用 `fallbackToDestructiveMigration()`，schema 变更会清空本地缓存。

## 发布前检查

- 确认 `local.properties`、签名文件、`.env`、IDE 配置和本地会话文件没有进入提交。
- 公开仓库中不要出现 service role key、个人私密配置或生产库敏感信息。
- 如真实 Supabase key 曾经被提交到远端仓库，建议在 Supabase 控制台轮换。
- release 签名只在本地配置，字段见 `local.properties.example`。
- 发布到 GitHub 前建议补充合适的开源许可证。
