# StandBy Us 视觉规范文档 v1.0

> 当前版本与现有 Android App 实现保持一致。项目技术栈为 Kotlin + Jetpack Compose + Material 3，视觉规范以代码中的 `Color.kt`、`Type.kt`、`AppHeader.kt`、各页面 `*Screen.kt` 与 `*Layout.kt` 为准。

## 一、产品气质

StandBy Us 是情侣状态共享与生活记录 App。整体视觉不是效率工具风，而是轻量、亲密、柔软的双人手账。

1. **暖色陪伴感**：以奶油白、珊瑚粉、暖灰棕为主，避免纯黑、高饱和正红、亮蓝等强刺激颜色。
2. **圆润低压**：卡片、按钮、输入框和头像都使用明显圆角，降低工具感。
3. **状态即情绪**：心情、贴纸、头像、打卡和照片是主要表达载体，允许 Emoji 和主题贴纸成为产品语言的一部分。
4. **轻动效反馈**：首页情绪卡、打卡按钮、弹窗和庆祝层使用轻量动效，强调陪伴与回应。
5. **中文优先**：所有用户可见文案使用简体中文，语气温柔、直接、生活化。

---

## 二、技术与页面范围

### 2.1 技术栈

- Android 原生应用，使用 Kotlin + Jetpack Compose。
- 主题基于 Material 3 `ColorScheme` 与自定义字体。
- 单模块 `:app`，主要导航目的地为：
  - `home`：首页 / 对方状态
  - `checkin`：拉了么 / 打卡
  - `album`：我们的相册
  - `history`：时光轴
  - `post_status`：发布状态
  - `settings`：设置

### 2.2 底部导航

底部导航只出现在主四页：`home`、`checkin`、`album`、`history`。

| 状态 | 规范 |
|------|------|
| 容器背景 | `Surface` / `#FFFFFF` |
| 顶部分割线 | `Border` / `#F0EAE6`，1dp |
| 图标尺寸 | 24dp |
| 标签字号 | 12sp，SemiBold |
| 选中态 | `Primary` / `#FFB4A2` |
| 未选中态 | `TextSecondary` / `#9E8E86` |
| 底部内边距 | 16dp，适配手势导航区域 |

---

## 三、全局色彩系统

### 3.1 主色板

当前 Material 3 主题的主色板定义在 `app/src/main/java/com/standbyus/app/ui/theme/Color.kt`。

| Token / 变量 | 色值 | 用途 |
|------|------|------|
| `Primary` | `#FFB4A2` | 主按钮、选中态、重点强调 |
| `PrimaryDark` | `#E5A292` | 主色深色派生 |
| `PrimaryLight` | `#FFD1C7` | 主色浅色派生 |
| `PrimarySoft` | `#FEF0EC` | 标签、徽章、浅强调背景 |
| `PrimaryBg` / `Background` | `#FFF8F5` | 默认页面背景 |
| `Secondary` | `#E8D5C4` | 奶茶色辅助强调 |
| `TertiaryColor` | `#D4E2D4` | 薄荷灰绿辅助色 |
| `Surface` | `#FFFFFF` | 卡片、Header、底部导航背景 |
| `SurfaceVariant` | `#FEF0EC` | 辅助表面、标签背景 |
| `TextPrimary` | `#5A4A42` | 主文字，替代纯黑 |
| `TextSecondary` | `#9E8E86` | 次要文字、普通图标 |
| `TextHint` | `#C4B5AD` | 占位、弱提示 |
| `Border` | `#F0EAE6` | 分割线、边框 |
| `BorderLight` | `#F9F7F5` | 更轻的边框 |
| `Shadow` | `#0F5A4A42` | 暖棕弥散阴影 |
| `Danger` | `#FF8575` | 危险、错误提示 |
| `Success` | `#6BCB77` | 成功反馈 |
| `Warning` | `#FFB347` | 警告反馈 |

### 3.2 首页背景

首页不是纯色背景，而是竖向柔和渐变，定义在 `HomeBackgroundStyle.kt`：

| 位置 | 色值 |
|------|------|
| 顶部 | `#FFF1EC` |
| 中段 | `#FFFAF7` |
| 底部 | `#F5F3FF` |

其他页面默认使用 `#FFF8F5` 作为背景。

### 3.3 深色模式

`Theme.kt` 已定义深色 `ColorScheme`，但设置页的“深色模式”开关当前是本地视觉状态，不会切换全局 App 主题。深色模式设计与实现应后续单独收敛，避免文档承诺超过现有行为。

| Token | 色值 |
|------|------|
| `DarkBg` | `#121212` |
| `DarkSurface` | `#1E1E1E` |
| `DarkSurfaceVariant` | `#2A2A2A` |
| `DarkTextPrimary` | `#FFFFFF` |
| `DarkTextSecondary` | `#AAAAAA` |
| `DarkTextHint` | `#777777` |
| `DarkBorder` | `#333333` |

---

## 四、字体系统

字体定义在 `Type.kt`。

| 用途 | 字体资源 | 说明 |
|------|------|------|
| 标题 / 展示 | `smiley_sans_oblique.otf` | 得意黑，用于页面标题、卡片标题、强调展示 |
| 正文 / 标签 | `misans_regular.otf` | MiSans，用于正文、按钮、输入、列表 |

### 4.1 Material 3 字体层级

| TextStyle | 字号 / 行高 | 字重 | 用途 |
|------|------|------|------|
| `displayLarge` | 32sp / 40sp | Bold | 大型展示文字 |
| `headlineLarge` | 24sp / 30sp | SemiBold | AppHeader 标题 |
| `headlineMedium` | 18sp / 24sp | Medium | 中型标题 |
| `titleLarge` | 20sp / 26sp | Bold | 卡片标题 |
| `titleMedium` | 16sp / 24sp | SemiBold | 重要正文、列表项 |
| `bodyLarge` | 16sp / 24sp | Normal | 默认正文 |
| `bodyMedium` | 14sp / 21sp | Normal | 辅助正文 |
| `labelLarge` | 14sp / 20sp | Medium | 按钮、标签 |
| `labelSmall` | 12sp / 18sp | Medium | 小标签、说明 |

### 4.2 文案原则

- 使用简体中文。
- 允许保留产品梗与亲密语气，例如“拉了么”“发布状态”“还没有绑定伴侣”。
- 错误、空状态、加载状态要给用户明确下一步。
- 亲密表达可以使用 Emoji，但系统级操作含义必须配合可访问描述，例如“设置”“上传照片”“刷新”“返回”。

---

## 五、形状、间距与阴影

### 5.1 圆角

当前圆角系统主要来自 `StandByUsTokens.kt` 与页面实现。

| 元素 | 当前常用值 |
|------|------|
| 小标签 / 小按钮 | 8dp |
| 输入框 / 中型卡片 | 12dp 或 16dp |
| 设置页 SectionCard | 16dp |
| 首页情绪主卡 / 打卡统计卡 | 24dp |
| 胶囊按钮 | 24dp 或 28dp |
| 底部弹窗顶部 | 20dp |
| 圆形头像 / 情绪点 / 打卡按钮 | `CircleShape` |

### 5.2 间距

全局使用 4dp / 8dp 节奏。

| 层级 | 值 |
|------|------|
| 极小间距 | 4dp |
| 小间距 | 8dp |
| 中间距 | 12dp |
| 常规内边距 | 16dp |
| 区块间距 | 20dp / 24dp |
| 大区块间距 | 32dp / 40dp / 48dp |

页面布局通过 `HomeLayout`、`CheckinLayout`、`AlbumLayout`、`HistoryLayout`、`PostStatusLayout`、`SettingsLayout` 做小屏适配。不要在页面里随意写死新的断点。

### 5.3 阴影

整体使用暖棕弥散阴影，避免硬黑阴影。

| 场景 | 当前实现 |
|------|------|
| 首页情绪主卡 | 8dp 阴影，主色低透明度 |
| 首页发布按钮 | 16dp 阴影，`#0F5A4A42` |
| 打卡 / 设置 / 时光轴卡片 | 16dp 阴影，`#0F5A4A42` 或 `#1F5A4A42` |
| 情绪选中项 | 12dp 阴影，使用情绪色 30% 透明度 |

---

## 六、顶部导航栏 AppHeader

公共组件：`app/src/main/java/com/standbyus/app/ui/components/AppHeader.kt`

| 属性 | 当前规范 |
|------|------|
| 背景色 | `#FFFFFF` |
| 高度 | 48dp |
| 水平内边距 | 4dp |
| 底部分割线 | 默认显示，`#F0EAE6`，1dp |
| 标题 | `MaterialTheme.typography.headlineLarge`，颜色 `#5A4A42`，绝对居中 |
| 返回图标 | Material `ArrowBack`，24dp，颜色 `#5A4A42` |
| 右侧图标 | 由页面传入，建议 24dp，颜色 `#9E8E86` |

`AppHeader` 参数：

```kotlin
fun AppHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null,
    showDivider: Boolean = true
)
```

当前页面使用：

| 页面 | 标题 | 左侧返回 | 右侧操作 | 分割线 |
|------|------|------|------|------|
| 首页 | `StandBy Us` | 无 | 设置 | 显示 |
| 拉了么 | `拉了么` | 无 | 无 | 不显示 |
| 相册 | `我们的相册` | 有 | 上传照片 | 显示 |
| 时光轴 | `时光轴` | 有 | 刷新 | 显示 |
| 发布状态 | `发布状态` | 有 | 无 | 不显示 |
| 设置 | `设置` | 有 | 无 | 显示 |

---

## 七、情绪、状态与贴纸

### 7.1 Feeling 枚举

当前 App 有 16 个情绪，定义在 `Feeling.kt`。

| Key | 显示名 | 默认 Emoji | 主色 | 渐变起始 | 渐变结束 |
|------|------|------|------|------|------|
| `happy` | 开心 | 😊 | `#FFD180` | `#FFD180` | `#FFAB40` |
| `missing` | 想你 | 🥰 | `#F8BBD0` | `#F8BBD0` | `#F48FB1` |
| `kiss` | 亲亲 | 😘 | `#E1BEE7` | `#E1BEE7` | `#CE93D8` |
| `love` | 爱你 | ❤️ | `#EF9A9A` | `#EF9A9A` | `#E57373` |
| `relaxed` | 悠闲 | 😌 | `#B2DFDB` | `#B2DFDB` | `#80CBC4` |
| `hustling` | 奋斗 | 💪 | `#FFE0B2` | `#FFE0B2` | `#FFCC80` |
| `thinking` | 思考 | 🤔 | `#FFF9C4` | `#FFF9C4` | `#FFF176` |
| `watching` | 追剧 | 📺 | `#B3E5FC` | `#B3E5FC` | `#81D4FA` |
| `sad` | 难过 | 😢 | `#BBDEFB` | `#BBDEFB` | `#90CAF9` |
| `upset` | 沮丧 | 😞 | `#D7CCC8` | `#D7CCC8` | `#BCAAA4` |
| `angry` | 生气 | 😡 | `#FFCDD2` | `#FFCDD2` | `#EF9A9A` |
| `anxious` | 焦虑 | 😰 | `#E0E0E0` | `#E0E0E0` | `#BDBDBD` |
| `tired` | 疲惫 | 😫 | `#C8E6C9` | `#C8E6C9` | `#A5D6A7` |
| `sick` | 生病 | 🤒 | `#DCEDC8` | `#DCEDC8` | `#C5E1A5` |
| `bored` | 无聊 | 😑 | `#F0F4C3` | `#F0F4C3` | `#E6EE9C` |
| `sleeping` | 睡觉 | 😴 | `#E8EAF6` | `#E8EAF6` | `#C5CAE9` |

兼容旧文案映射：`想你了` -> `想你`，`爱心` -> `爱你`，`看剧` -> `追剧`，`委屈` / `哭哭` -> `沮丧`。

### 7.2 Doing 枚举

当前保留 12 个行为选项，定义在 `Doing.kt`：

`搬砖`、`加班`、`学习`、`睡觉`、`干饭`、`运动`、`通勤`、`看电影`、`玩游戏`、`发呆`、`桌游`、`自定义`。

发布状态页目前以“表情贴纸 / 心情底色 / 常用短句 / 补充文本”为主要输入体验，`DoingPicker` 是保留组件，不是当前发布页主流程。

### 7.3 主题贴纸

App 支持动态表情主题：

- 默认主题使用 Emoji。
- 非默认主题可从 Supabase Storage 加载贴纸资源。
- `StatusEmojiImage` 负责在 Emoji、远程图片和 fallback 之间切换。
- 发布状态页在主题有贴纸时优先显示 4 列贴纸网格，再显示横向心情底色选择。

---

## 八、页面规范

### 8.1 首页 `home`

首页由 `HomeScreen.kt`、`HomeLayout.kt` 与多个 `Home*Style.kt` 控制。

| 区块 | 当前实现 |
|------|------|
| 背景 | 竖向柔和渐变：`#FFF1EC`、`#FFFAF7`、`#F5F3FF` |
| Header | 标题 `StandBy Us`，右侧设置图标，24dp，`#9E8E86` |
| 对方状态卡 | 24dp 圆角，情绪分组线性渐变，白色描边光感，8dp 主色阴影 |
| 情绪图像 | 随可用高度缩放，最大约 180dp，带 3s 上下漂浮动效 |
| 状态文字 | 主要行为白色加粗，心情白色 90% 透明度，备注为斜体白色 80% |
| 互动区 | 与主卡轻微重叠，24dp 底部圆角，半透明暖色渐变 |
| 最新互动提示 | 18dp 圆角，`#FFF1ED` 背景，`#33FFB4A2` 边框 |
| 我的状态条 | 16dp 圆角，`#FFFFFAF0` 背景，左侧 48dp 表情，右侧 Chevron |
| 互动按钮 | 2 列网格，42dp 高，18dp 圆角，四色浅背景轮换 |
| 发布按钮 | 底部固定，56dp 高，28dp 胶囊，`#FFB4A2` 背景，文字“✨ 发布状态” |
| 空状态 | Emoji + “还没有绑定伴侣” + “去绑定”按钮 |

首页布局会根据屏幕宽高动态压缩主卡高度、表情尺寸、间距和上下留白。

### 8.2 拉了么 `checkin`

当前实现是生活化打卡页，包含 PK 卡、统计卡和大圆形打卡按钮。

| 区块 | 当前实现 |
|------|------|
| 背景 | `#FFF8F5` |
| Header | 标题 `拉了么`，无返回，无分割线 |
| PK 卡 | 白色 24dp 圆角卡片，16dp 弥散阴影，宽度为屏宽 90%，限制在 288-420dp |
| PK 内容 | “本月拉屎大王对决”，左右 `我` / `TA`，中间 `VS` 或握手 |
| 进度条 | 8dp 高，4dp 圆角，底色 `#F0EAE6` |
| 统计卡 | 两张三列卡，白底 24dp 圆角，内部 1dp 竖向分割线 |
| 统计图标 | Emoji 放在 36dp 圆形浅色背景中 |
| 主按钮 | 100-118dp 圆形，径向渐变 `#FFB4A2` -> `#FF9E8E` |
| 主按钮动效 | 三层 3s 循环波纹，点击时禁用重复提交 |

### 8.3 我们的相册 `album`

当前实现是 3 列正方形照片网格，不是瀑布流。

| 区块 | 当前实现 |
|------|------|
| Header | 标题 `我们的相册`，左侧返回，右侧上传图标 |
| 筛选 | 横向滚动标签：`全部`、`近7天`、`近30天`、`近90天` |
| 筛选选中态 | 20dp 胶囊，`#FFB4A2` 背景，白色文字 |
| 筛选未选中态 | 透明背景，`#F0EAE6` 边框，`#9E8E86` 文字 |
| 图片网格 | `LazyVerticalGrid`，3 列固定，间距 6-8dp |
| 图片卡 | 正方形，12dp 圆角，点击全屏查看，长按删除确认 |
| 图片标题 | 底部 48dp 黑色渐变遮罩，白色 11sp 加粗单行截断 |
| 上传弹窗 | 底部 Sheet，顶部 20dp 圆角，40% 黑色遮罩 |
| 上传预览 | 132-160dp 正方形，12dp 圆角 |
| 输入框 | “写一句小记（可选）”，16dp 圆角 |
| 发布按钮 | 46-48dp 高，28dp 胶囊，`#FFB4A2` |
| 空状态 | 📷 + “还没有照片” + “点击右上角上传第一张” |
| 全屏查看 | 黑色背景，支持双指缩放与拖动，点击图片退出 |

### 8.4 时光轴 `history`

时光轴采用左右对话式时间记录，而不是连续竖线时间轴。

| 区块 | 当前实现 |
|------|------|
| 背景 | `#FFF8F5` |
| Header | 标题 `时光轴`，左侧返回，右侧刷新 |
| 日期标题 | 14sp，700 字重，`#9E8E86`，1sp 字距，大写显示 |
| 条目布局 | 根据是否为自己决定左右排列 |
| 条目卡片 | 白色 16dp 圆角，16dp 弥散阴影 |
| 条目旋转 | 按时间戳奇偶轻微旋转 +1deg / -1deg |
| 情绪点 | 36-40dp 圆形，白色 2dp 描边，内含状态表情 |
| 情绪徽章 | 圆形浅色情绪背景，含表情与 9sp 情绪名 |
| 文本 | 人名 14sp Medium，时间 12sp，状态 14sp，备注 13sp 斜体 |
| 空状态 | 📭 + “还没有状态记录” + “分享你的第一个瞬间吧” |
| 加载 | 有历史数据时顶部显示线性进度条 |

### 8.5 发布状态 `post_status`

发布状态页是当前最复杂的输入页，适配默认 Emoji 与动态主题贴纸两种模式。

| 区块 | 当前实现 |
|------|------|
| 背景 | 根据选中情绪生成顶部 8% 透明色，再渐变到 `#FFF8F5` |
| Header | 标题 `发布状态`，左侧返回，无分割线 |
| 贴纸模式 | 主题有贴纸时显示 4 列贴纸网格 |
| 默认模式 | 无贴纸时显示 4 列情绪网格，16 个情绪，固定 4 行 |
| 情绪选中 | 圆形放大到 1.12 倍，白色 3dp 描边 + 情绪色 2dp 描边 |
| 常用短句 | 横向滚动 Chip，18dp 圆角，白底，情绪色渐变边框 |
| 补充输入 | 16dp 圆角 OutlinedTextField，占位“想补充点什么...” |
| 发布按钮 | 底部固定，56dp 高，28dp 胶囊，`#FFB4A2` |
| 发布反馈 | 发布中显示白色进度圈，成功显示“已发布！”并 1s 后返回 |
| 错误反馈 | 页面中部红色错误文案，14sp，居中 |

输入区域使用 `navigationBarsPadding()` 与 `imePadding()`，避免被系统导航栏和键盘遮挡。

### 8.6 设置 `settings`

设置页由多张 SectionCard 组成，承载头像、配对、外观、关于和特效预览。

| 区块 | 当前实现 |
|------|------|
| 背景 | `#FFF8F5` |
| Header | 标题 `设置`，左侧返回 |
| SectionCard | 白色 16dp 圆角，16dp 弥散阴影，内边距 16-20dp |
| SectionTitle | 13sp Bold，`#9E8E86`，0.5sp 字距 |
| 个人头像 | 56-64dp 圆形头像，点击打开头像选择弹窗 |
| 配对状态 | 未配对时生成配对码 / 输入 6 位配对码；已配对时可改对方昵称、断开连接 |
| 配对码 | 28sp Bold，`#FFB4A2`，4sp 字距 |
| 输入框 | 16dp 圆角，聚焦边框 `#FFB4A2`，未聚焦 `#F0EAE6` |
| 外观 | 深色模式本地开关 + 动态主题列表 |
| Toggle | 52x28dp，14dp 圆角，thumb 24dp，200ms 动画 |
| 主题行 | 40dp 图标块，10dp 圆角，选中显示 `✓` |
| 关于 | 版本、产品描述、字体说明、特效预览入口 |
| 弹窗 | 头像选择 Dialog，20dp 圆角，最大高度 320-400dp |

---

## 九、动效与交互

| 场景 | 当前规范 |
|------|------|
| 首页情绪图像漂浮 | 3s，`EaseInOutCubic`，上下 8dp，循环往返 |
| 首页互动按钮 loading | 按钮禁用，显示 16dp 进度圈 |
| 发布页背景色变化 | 400ms 颜色动画 |
| 发布页选中情绪 | scale 1.12，阴影与描边强调 |
| 发布成功 | 显示“已发布！”，延迟 1s 返回上一页 |
| 打卡按钮波纹 | 3 层 3s 循环圆形波纹 |
| 设置 Toggle | 200ms thumb 位移动画 |
| 图片加载 | Coil crossfade |
| 全屏图片 | 支持双指缩放，缩放范围 1x-5x |
| 庆祝层 | 启动时按日期触发，每天只显示一次；配对成功也会触发爱心特效 |

交互控件应尽量使用 `Surface(onClick)`、`Button`、`IconButton`、`OutlinedButton` 等 Compose 语义组件。自定义 `Box.clickable` 需要保证尺寸不小于 44-48dp，并提供可访问描述。

---

## 十、组件清单

### 10.1 已有公共组件

| 组件 | 位置 | 用途 |
|------|------|------|
| `AppHeader` | `ui/components/AppHeader.kt` | 全局顶部导航 |
| `StatusEmojiImage` | `ui/components/StatusEmojiImage.kt` | Emoji / 远程图片状态图统一渲染 |
| `StatusCard` | `ui/components/StatusCard.kt` | 通用状态卡，保留组件 |
| `FeelingPicker` | `ui/components/FeelingPicker.kt` | 通用情绪选择，保留组件 |
| `DoingPicker` | `ui/components/DoingPicker.kt` | 通用状态输入，保留组件 |
| `AvatarPicker` | `ui/components/AvatarPicker.kt` | 头像选择 |
| `AvatarWithGlow` | `ui/components/AvatarWithGlow.kt` | 发光头像 |

### 10.2 页面内组件

部分组件目前仍在页面文件内实现，例如：

- 首页：`PartnerStatusCard`、`HomeInteractionFlow`、`InteractionActionButton`、`QuickPostButton`
- 打卡：`PKCard`、`ThreeColCard`、`BigCheckinButton`
- 相册：`PhotoCard`、上传 Sheet、全屏查看层
- 时光轴：`TimelineEntry`、`MoodBadge`
- 发布状态：`MoodGridPicker`、`MoodRowPicker`、`StickerGridPicker`、`PhraseSuggestionChips`
- 设置：`SectionCard`、`ToggleRow`、`ThemeRow`、`InlineAvatarPicker`

后续如需组件化，应优先抽出重复度高、视觉规则稳定的组件，不要为了形式统一拆分一次性页面结构。

---

## 十一、可访问性与适配

1. 主要点击区域应保持不小于 44-48dp。
2. Icon-only 操作必须提供 `contentDescription`，例如“返回”“设置”“上传照片”“刷新”。
3. 页面底部固定按钮必须避开导航栏与键盘；发布页已使用 `navigationBarsPadding()` 与 `imePadding()`。
4. 图片说明使用 `contentDescription = photo.caption`，无说明时可为空。
5. 禁止仅依靠颜色表达关键状态；需要搭配文本、图标或位置变化。
6. 小屏适配优先修改对应 `*Layout.kt`，不要在 Composable 内新增零散魔法值。
7. 动态文字放大时，按钮和卡片应允许文本换行或缩短文案，避免溢出。

---

## 十二、QA 检查项

提交视觉相关改动前确认：

- [ ] 新页面使用 `AppHeader`，标题、返回、右侧图标颜色与现有页面一致。
- [ ] 页面背景符合当前规则：首页渐变，其他主页面默认 `#FFF8F5`。
- [ ] 主文字避免纯黑，优先使用 `TextPrimary` / `#5A4A42`。
- [ ] 主要按钮使用 `Primary` / `#FFB4A2`，并保持 24-28dp 胶囊圆角。
- [ ] 卡片使用白色表面、16-24dp 圆角和暖棕弥散阴影。
- [ ] 情绪、贴纸、头像相关 UI 能同时处理 Emoji 与远程图片。
- [ ] 相册仍保持当前 3 列正方形网格，除非产品明确要改为瀑布流。
- [ ] 发布页底部按钮不被键盘或系统导航遮挡。
- [ ] 所有图标按钮有语义描述。
- [ ] 空状态文案清楚说明现状和下一步。
- [ ] 视觉改动在小屏宽度 `<360dp` 与较低高度 `<600dp` 下不会遮挡或溢出。

---

**文档版本**：v1.0  
**更新日期**：2026-06-01  
**基准实现**：当前 Android App（Kotlin + Jetpack Compose）  
**主要代码来源**：`Color.kt`、`Type.kt`、`AppHeader.kt`、`HomeScreen.kt`、`CheckinScreen.kt`、`AlbumScreen.kt`、`HistoryScreen.kt`、`PostStatusScreen.kt`、`SettingsScreen.kt`
