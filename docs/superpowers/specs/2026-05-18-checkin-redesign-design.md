# Checkin 打卡页面重新设计 + PK 对决

**日期**: 2026-05-18
**状态**: 设计确认 ✅

---

## 1. 概述

将底部导航「打卡」(CheckinScreen) 页面从纯 UI 存根改造为完整的打卡统计 + PK 对决页面。参考 `E:\AndroidProject\UI设计\StandByUs Mobile UI Design\打卡页面预览.html` 的 UI 布局，适配 StandBy Us 珊瑚暖色系统。

### 功能目标

1. **打卡记录**：点击大按钮记录一条打卡记录（💩），保存到本地（Room）+ 同步到 Supabase
2. **今日统计**：显示今日已记录次数、首次打卡时间、便秘风险
3. **本周统计**：显示本周总共次数、日均次数、连续打卡天数
4. **PK 对决**：与伴侣比拼本月打卡次数，显示胜负关系，获胜方获得"拉屎大王"🏆 称号
5. **数据同步**：沿用现有 StatusRepository 轮询模式（Supabase REST + 6s callbackFlow），打卡记录在双方设备间同步

---

## 2. 页面布局（自顶向下）

```
┌───────────────────────────────────────┐
│  AppHeader("拉了么")            [⚙️]  │  ← 无返回按钮（底部导航标签页）
├───────────────────────────────────────┤
│  ┌─ 滚动区域 (Scrollable) ────────┐  │
│  │                                 │  │
│  │  "保持冷静，继续拉"              │  │  ← 暖橙 #F09B6D, 18sp Medium
│  │  📅 2026年5月18日 周一          │  │  ← 暖灰 #9E8E86, 14sp
│  │                                 │  │
│  │  ┌── 今天 ─────────────────┐   │  │
│  │  │  💩 3次  ⏰ 08:23  😊正常 │   │  │  ← 3 列白色卡片 + 弥散阴影
│  │  └─────────────────────────┘   │  │
│  │                                 │  │
│  │  ┌── 本周 ─────────────────┐   │  │
│  │  │ 📊 12次  📈 1.7/天 🔥5天 │   │  │
│  │  └─────────────────────────┘   │  │
│  │                                 │  │
│  │  ┌── PK 对决 ─────────────┐   │  │  ← 仅在已配对时显示
│  │  │  🏆 本月拉屎大王对决     │   │  │
│  │  │                         │   │  │
│  │  │  我        VS       TA   │   │  │
│  │  │  12次 👑            8次  │   │  │
│  │  │  ████████████░░░░░░░░░░  │   │  │
│  │  │  领先 4 次 🎉            │   │  │
│  │  └─────────────────────────┘   │  │
│  │                                 │  │
│  └─────────────────────────────────┘  │
│                                         │
│  ┌─ 固定底部 (非滚动) ──────────────┐  │
│  │       ╭──────────────╮          │  │
│  │       │     💩        │          │  │  ← 200dp珊瑚渐变圆形按钮
│  │       │ 今天也顺利啦   │          │  │     + 水波纹动画
│  │       ╰──────────────╯          │  │
│  └─────────────────────────────────┘  │
│  ┌─ Bottom Nav ────────────────────┐  │
│  │ Home │ 拉了么 │ Album │ Timeline │  │  ← 保持不变
│  └─────────────────────────────────┘  │
└───────────────────────────────────────┘
```

### 2.1 配色映射

| HTML 设计色 | → 替换为 | Color.kt Token |
|---|---|---|
| 薄荷绿 `#7DD3C0` | 珊瑚 `#FFB4A2` | `Primary` |
| 浅绿背景 `#F5F9F7` | 奶油白 `#FFF8F5` | `Background` |
| 卡片白色 | 白色 `#FFFFFF` | `Surface` |
| 文字黑色 | 暖棕黑 `#5A4A42` | `TextPrimary` |
| 辅助文字 | 暖灰 `#9E8E86` | `TextSecondary` |
| 副标题暖橙 | `#F09B6D` | —（新 token） |
| 获胜方徽章 | `#FFE8B84B` | 金色 |
| 高风险警告 | `#FFE07070` | 珊瑚红 |

### 2.2 头部

- **左侧**：无返回按钮（底部导航标签页，不需要 popBackStack）
- **右侧**：设置齿轮图标，暖灰 `#9E8E86`，点击导航到 SettingsScreen（可选 — 为了干净布局可以去掉）
- **标题**："拉了么"，居中，18sp Bold，`#5A4A42`

### 2.3 副标题区域

- 第一行："保持冷静，继续拉"，`#F09B6D`（暖橙），18sp Medium
- 第二行：当前日期，如"📅 2026年5月18日 周一"，`#9E8E86`（暖灰），14sp

### 2.4 主打卡按钮

| 属性 | 值 |
|---|---|
| **尺寸** | 200dp 圆形 |
| **背景** | `Brush.radialGradient` 珊瑚 `#FFB4A2` → `#FF9E8E` |
| **Emoji** | 💩 56sp |
| **文案** | "今天也顺利啦 🎉", 20sp, 白色, Bold |
| **阴影** | 弥散阴影, `ambientColor = Color(0x3F5A4A42)`, 20dp elevation, CircleShape |
| **动画** | 3 圈水波纹, 每圈 3s, 间隔 1s 启动, scale 1.0→1.5, opacity 0.4→0 |
| **按压反馈** | 缩放 0.95 + 触觉反馈, 100ms ease-out |
| **位置** | 水平居中, 固定在底部导航上方（非滚动区域, 适合拇指操作） |

### 2.5 统计卡片

两行独立卡片（非表格），每行 3 列等宽，白色背景，24dp RoundedCornerShape，弥散阴影：

#### 今天卡片

| 列 | 内容 | 数据来源 |
|---|---|---|
| ① | 💩 **N 次** / "已记录" | Room COUNT of today's records |
| ② | ⏰ **HH:mm** / "时间" | 首次打卡时间（MIN timestamp）或 "—" |
| ③ | 😊 **正常** / "便秘风险" | 根据距上次打卡时间计算 |

#### 本周卡片

| 列 | 内容 | 数据来源 |
|---|---|---|
| ① | 📊 **N 次** / "总共" | Room COUNT of this week's records |
| ② | 📈 **N/天** / "平均" | 总数 ÷ 本周已过天数，保留 1 位小数 |
| ③ | 🔥 **N 天** / "连续打卡" | 从今天往前数最大连续天数 |

#### 便秘风险等级

| 距上次打卡时间 | 等级 | 显示 |
|---|---|---|
| 今天打过卡 | NORMAL | 正常 😊 |
| 1 天没打 | MILD | 轻度 🤔 |
| 2 天+没打 | ATTENTION | 注意 😰 |

---

## 3. PK 对决

### 3.1 PK 规则

- 按**自然月**统计双方（自己 vs 伴侣）的打卡次数
- 次数多者胜出，获得"🏆 拉屎大王"称号 + 金色徽章
- 平局显示"🤝 不分胜负"
- 每月 1 日 00:00 自动重置

### 3.2 PK 数据流

```
CheckinViewModel.init()
  → resolveMyUserId() + resolvePartnerUserId()
  → observeCheckIns(myId, monthStart) → computePKStats()
  → observeCheckIns(partnerId, monthStart) → computePKStats()
  → PKStats(myCount, partnerCount, winner, leadAmount, catchUpRate)
```

Partner 的 userId 通过以下方式解析：
1. 优先从 `SharedPreferences("pairing").partner_id` 缓存读取
2. 无缓存时调用 `PairingRepository.findPairByUserId()` 联网查询
3. 最终结果缓存到 SharedPreferences

### 3.3 PK 区域布局（仅在已配对时显示）

```
┌───────────────────────────────────────────┐
│  🏆 本月拉屎大王对决                       │
│                                           │
│      我           VS           TA          │
│     👑 12次                 8次            │
│    ████████████████░░░░░░░░░░░░░░░░░░░░    │
│                                           │
│    🎉 领先 4 次！                          │
│    距离月底还有 12 天                      │
│    TA 每天需要多拉 0.33 次才能追上          │
│                                           │
└───────────────────────────────────────────┘
```

**无伴侣时的占位状态：**
```
┌───────────────────────────────────────────┐
│  🏆 本月拉屎大王对决                       │
│                                           │
│    绑定伴侣后即可与 TA PK                  │
│                  [去绑定]                  │
└───────────────────────────────────────────┘
```

### 3.4 实时更新

- 自己打卡后 → 本地 Room 写入 → ViewModel 自动刷新 → PK 重新计算
- 每 6s 轮询 Supabase → 拉取对方打卡记录 → 更新本地 Room 缓存 → PK 重新计算
- 无需手动刷新

---

## 4. 数据层设计

### 4.1 Supabase 新表: `checkins`

沿用 `statuses` 表设计模式（BIGSERIAL PK + TEXT userId + BIGINT timestamp）。

| 列名 | 类型 | 默认值 | 说明 |
|---|---|---|---|
| `id` | `BIGSERIAL` PK | auto | 自增主键 |
| `userId` | `TEXT` | `''` | 设备 UUID |
| `timestamp` | `BIGINT` | `0` | 打卡时间戳（epoch ms） |
| `note` | `TEXT` | `''` | 备注（预留） |

### 4.2 Room 新表: `checkin_records`

```kotlin
@Entity(tableName = "checkin_records")
data class CheckinRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Long,      // System.currentTimeMillis()
    val note: String = ""
)
```

### 4.3 DAO 查询

```kotlin
@Dao
interface CheckinDao {
    @Query("SELECT COUNT(*) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfDay")
    suspend fun getTodayCount(userId: String, startOfDay: Long): Int

    @Query("SELECT MIN(timestamp) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfDay")
    suspend fun getFirstCheckinTime(userId: String, startOfDay: Long): Long?

    @Query("SELECT MAX(timestamp) FROM checkin_records WHERE userId = :userId")
    suspend fun getLastCheckinTime(userId: String): Long?

    @Query("SELECT COUNT(*) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfWeek")
    suspend fun getWeekCount(userId: String, startOfWeek: Long): Int

    @Query("SELECT COUNT(*) FROM checkin_records WHERE userId = :userId AND timestamp >= :startOfMonth")
    suspend fun getMonthCount(userId: String, startOfMonth: Long): Int

    @Query("SELECT DISTINCT CAST(timestamp / 86400000 AS INTEGER) FROM checkin_records " +
           "WHERE userId = :userId AND timestamp >= :since " +
           "ORDER BY timestamp DESC")
    suspend fun getActiveDays(userId: String, since: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: CheckinRecordEntity)

    @Query("DELETE FROM checkin_records WHERE userId = :userId")
    suspend fun clearUserRecords(userId: String)
}
```

### 4.4 CheckinRepository

复制 `StatusRepository` 模式，数据流向：

```
写入：submitCheckIn()
  → ① SupabaseService.create("checkins", data)   // POST 到 Supabase
  → ② CheckinDao.insert(entity)                   // Room 缓存

读取轮询：observeCheckIns(userId, since)
  → callbackFlow {
      fetchFromRemote()         // 立即查 Supabase
      → if ok: cache to Room + trySend
      → if fail: trySend from Room (fallback)

      delay(6000)
      while (isActive) {
        fetchFromRemote()
        delay(6000)
      }
    }
```

### 4.5 Supabase API 查询

查询对方打卡记录（与 `StatusRepository.observeStatus` 相同模式）：

```
GET /rest/v1/checkins?userId=eq.{partnerId}&timestamp=gte.{monthStart}&order=timestamp.desc
```

### 4.6 数据模型

```kotlin
data class CheckinData(
    val id: Long = 0,
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "timestamp" to timestamp,
        "note" to note
    )
    companion object {
        fun fromMap(map: Map<String, Any>): CheckinData { ... }
    }
}

data class PKStats(
    val myCount: Int,
    val partnerCount: Int
) {
    val winner: String?  // null=tie, "me", "partner"
    val leadAmount: Int  // 领先次数
    val myPercentage: Float  // 0.0~1.0
    val daysUntilMonthEnd: Int
    val partnerCatchUpRate: Float  // 对方每天需多拉几次才能追
}
```

### 4.7 CheckinUiState

```kotlin
data class CheckinUiState(
    val todayCount: Int = 0,
    val firstCheckinTime: String = "—",
    val riskLevel: RiskLevel = RiskLevel.NORMAL,
    val weeklyTotal: Int = 0,
    val weeklyAverage: Float = 0f,
    val streak: Int = 0,
    val isCheckingIn: Boolean = false,
    val isPaired: Boolean = false,
    val pkStats: PKStats? = null,
    val partnerId: String = ""
)

enum class RiskLevel(val label: String, val emoji: String) {
    NORMAL("正常", "😊"),
    MILD("轻度", "🤔"),
    ATTENTION("注意", "😰")
}
```

---

## 5. 打卡流程

1. 用户点击大按钮 → `CheckinViewModel.checkin()`
2. ViewModel: `_isCheckingIn = true` → 触觉反馈
3. `CheckinRepository.submitCheckIn()`:
   - 写入 Supabase → 写入 Room
   - 成功后重新查询 Room → 更新 UI State
4. ViewModel: `_isCheckingIn = false`
5. 按钮缩放回弹动画（1.0 → 0.95 → 1.05 → 1.0）

---

## 6. 交互与动画

| 交互 | 效果 |
|---|---|
| 主按钮点击 | 缩放 0.95 + 触觉反馈, 100ms ease-out, 然后回弹 |
| 水波纹动画 | `infiniteRepeatable`, 3 圈波纹, 每圈 3s, 间隔 1s, 从圆心向外扩散 |
| 卡片静态 | 白色背景 24dp 圆角 + 弥散阴影（无按压交互） |
| PK 区域 | 每次数据更新时数值渐变（可选） |

---

## 7. 文件改动清单

### 新增文件

| 文件 | 路径 | 说明 |
|---|---|---|
| `CheckinData.kt` | `app/.../data/model/` | Supabase 映射数据类 |
| `CheckinDao.kt` | `app/.../data/local/` | Room DAO 接口 |
| `CheckinRecordEntity.kt` | `app/.../data/local/` | Room Entity + mapper 函数 |
| `CheckinRepository.kt` | `app/.../data/repository/` | 数据同步仓库 |
| `CheckinViewModel.kt` | `app/.../ui/checkin/` | 页面 ViewModel |

### 修改文件

| 文件 | 变更内容 |
|---|---|
| `AppDatabase.kt` | 添加 `CheckinRecordEntity` entity + `checkinDao()` |
| `CheckinScreen.kt` | **重写** — 新布局 + PK 区域 + 打卡按钮动画 |
| `AppModule.kt` | 添加 `CheckinDao` 的 `@Provides` 方法 |

### 不变文件

- `MainActivity.kt` — 导航路由 + BottomNav 不变
- `Routes.kt` — 已有 `CHECKIN` 路由，不变
- `SupabaseService.kt` — 通用 CRUD 方法，无需改动
- `PairingRepository.kt` — 复用现有 `findPairByUserId()`
- 所有其他文件

---

## 8. 验收标准

- [ ] 点击打卡按钮 → Room 插入记录 + Supabase create 成功
- [ ] 今日统计实时更新：次数 / 首次时间 / 便秘风险
- [ ] 本周统计正确计算：总数 / 日均 / 连续打卡
- [ ] 首次使用（无数据）全部显示空状态（0 次 / — / 😊正常）
- [ ] 绑定伴侣后，PK 区域显示双方本月打卡数据
- [ ] 无伴侣时 PK 区域显示"绑定伴侣后即可PK"
- [ ] 领先方显示"拉屎大王"徽章 + 金色配色
- [ ] 打卡按钮有径向渐变 + 水波纹动画 + 按下缩放反馈
- [ ] 打卡按钮底部固定，不随内容滚动
- [ ] AppHeader 无返回按钮（顶部导航标签页）
- [ ] 配色使用 Color.kt tokens + 设计文档指定色值
- [ ] 全部字符串使用简体中文
- [ ] `fallbackToDestructiveMigration()` 自动处理数据库迁移

---

## 9. 实现顺序建议

1. 数据层：`CheckinData` → `CheckinRecordEntity` → `CheckinDao` → `AppDatabase` 添加 → `AppModule` 绑定
2. 仓库层：`CheckinRepository`（含 Supabase 写入 + Room 缓存 + callbackFlow polling）
3. ViewModel：`CheckinViewModel`（UI State 整合 + PK 计算）
4. UI 层：重写 `CheckinScreen.kt`（布局 + PK 区域 + 动画）
5. 验证：build → 手动测试全部交互

---

## 10. 不在此次范围内

- 底部导航栏样式修改（保持现有）

## 修改历史

| 版本 | 日期 | 变更 |
|---|---|---|
| v1 | 2026-05-18 | 初始设计（不含 PK） |
| v2 | 2026-05-18 | 加入 PK 对决功能 + Supabase 同步 + 月度拉屎大王评选 |
