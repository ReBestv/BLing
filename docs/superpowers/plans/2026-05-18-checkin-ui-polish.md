# Checkin 打卡页面 UI 调整 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 缩小打卡按钮（200dp→100dp）、PK 对决移到顶部、去掉日期/副标题、今日统计改为间隔

**Architecture:** 纯 UI + ViewModel + DAO 改动，不涉及数据层仓库或数据库迁移

**Tech Stack:** Kotlin, Jetpack Compose, Room (DAO), Hilt ViewModel

---

### Task 1: CheckinDao — 新增最近两次打卡时间查询

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/data/local/dao/CheckinDao.kt`

- [ ] **Step 1: 添加 getLastTwoCheckinTimes 查询**

在 `CheckinDao.kt` 中添加：

```kotlin
@Query("SELECT timestamp FROM checkin_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 2")
suspend fun getLastTwoCheckinTimes(userId: String): List<Long>
```

放在 `getLastCheckinTime` 方法之后即可。这返回最多 2 条时间戳，按从新到旧排序。

---

### Task 2: CheckinViewModel — 替换间隔字段 + 清理 UI state

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/ui/checkin/CheckinViewModel.kt`

- [ ] **Step 1: 修改 CheckinUiState — 替换字段**

```kotlin
data class CheckinUiState(
    val todayCount: Int = 0,
    val lastInterval: String = "—",    // 替换 firstCheckinTime
    val riskLevel: RiskLevel = RiskLevel.NORMAL,
    val weeklyTotal: Int = 0,
    val weeklyAverage: Float = 0f,
    val streak: Int = 0,
    val isCheckingIn: Boolean = false,
    val isPaired: Boolean = false,
    val pkStats: PKStats? = null
    // 移除: firstCheckinTime, subtitle, dateDisplay
)
```

- [ ] **Step 2: 添加间隔计算函数**

```kotlin
private fun formatInterval(lastTwoTimestamps: List<Long>): String {
    if (lastTwoTimestamps.size < 2) return "—"
    val diff = lastTwoTimestamps[0] - lastTwoTimestamps[1]
    val hours = diff / 3_600_000
    val minutes = (diff % 3_600_000) / 60_000
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}
```

- [ ] **Step 3: 更新 combine 块中的数据获取和 UiState 构造**

在 `uiState` 的 `combine` 块中：

```kotlin
// 替换原有的 firstCheckinTime 获取
val lastTwoTimestamps = checkinRepository.getLastTwoCheckinTimes(myId)
val interval = formatInterval(lastTwoTimestamps)

// 构建 UiState（移除 subtitle/dateDisplay）
CheckinUiState(
    todayCount = todayCount,
    lastInterval = interval,
    riskLevel = riskLevel,
    weeklyTotal = weekTotal,
    weeklyAverage = weekAverage,
    streak = streak,
    isCheckingIn = checkingIn,
    isPaired = partnerId.isNotEmpty(),
    pkStats = pkStats
)
```

注意：将 `formatDateDisplay()` 调用完全移除，将 `formatDateDisplay()` 方法也一并删除。

- [ ] **Step 4: 删除不再需要的 private 方法**

删除：
- `formatDateDisplay()` 方法

`formatTime()` 方法如果其他地方不再使用也一并删除（但 `other` 方法可能还在用，确认一下 — 实际上 `firstCheckinTime` 移除了，`formatTime` 不再被引用，可以删除）。

---

### Task 3: CheckinScreen — 重排布局 + 缩小按钮

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/ui/checkin/CheckinScreen.kt`

- [ ] **Step 1: 重排滚动区域内容顺序**

关键改动：
1. 删除 `dateDisplay` 的 Text composable（原有约第 104-108 行）
2. 删除 subtitle 的 Text composable（原有约第 96-102 行）
3. 将 PK 区域（`PKCard`/`EmptyPKCard`，原约第 131-135 行）移到 今日统计卡片 **之前**

新的滚动区域顺序：
```
Column {
    // PK Section (moved to top)
    if (state.isPaired && state.pkStats != null) {
        PKCard(stats = state.pkStats!!)
    } else {
        EmptyPKCard()
    }
    Spacer(...)

    // Today Stats Card
    ThreeColCard(
        col1 = { StatCell("💩", "${state.todayCount} 次", "已记录", ...) },
        col2 = { StatCell("⏰", state.lastInterval, "间隔", Color(0xFFE3F2FD)) },
        col3 = { StatCell(state.riskLevel.emoji, state.riskLevel.label, "便秘风险", ...) }
    )
    Spacer(...)

    // This Week Stats Card (unchanged)
    ThreeColCard(...)
    Spacer(...)
}
```

- [ ] **Step 2: 缩小打卡按钮到 100dp**

修改 `BigCheckinButton`：
- 按钮尺寸从 `Modifier.size(200.dp)` 改为 `Modifier.size(100.dp)`
- Emoji 从 `56.sp` 改为 `40.sp`
- 去掉文案 `"今天也顺利啦 🎉"` — 只保留 💩 emoji
- 按钮内部 Column 调整间距：

```kotlin
Box(
    modifier = modifier
        .size(100.dp)      // 200 → 100
        .drawBehind { ... }  // 水波纹动画保持不变
        .shadow(...)
        .clip(CircleShape)
        .background(Brush.radialGradient(...))
        .clickable(enabled = !isCheckingIn) { onClick() },
    contentAlignment = Alignment.Center
) {
    Text(text = "💩", fontSize = 40.sp)   // 只保留 emoji
}
```

- [ ] **Step 3: 更新 StateCell 中 second column 的 label**

将 `ThreeColCard` 第 2 列的 label 从 `"时间"` 改为 `"间隔"`：

```kotlin
col2 = { StatCell("⏰", state.lastInterval, "间隔", Color(0xFFE3F2FD)) }
```

注意引用 `state.lastInterval` 而不是 `state.firstCheckinTime`.

- [ ] **Step 4: 移除不再需要的 import**

如果编译后发现某些 import 不再使用（如 `SimpleDateFormat` 相关的可能在 ViewModel 中），一并清除。

---

### Task 4: 验证 Build

- [ ] **Step 1: 运行 build**

```bash
cd E:\AndroidProject\StandByUs
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL` — 无编译错误。

---

### 范围检查

| Spec 要求 | 对应 Task | 状态 |
|---|---|---|
| 按钮 200dp → 100dp，仅 💩 emoji | Task 3 Step 2 | ✅ |
| 按钮保持径向渐变 + 水波纹 | Task 3 Step 2 | ✅ |
| 按钮底部固定，不在滚动区 | 完全不动（布局结构不变） | ✅ |
| PK 对决移到顶部 | Task 3 Step 1 | ✅ |
| 日期显示移除 | Task 3 Step 1 | ✅ |
| 副标题移除 | Task 3 Step 1 + Task 2 Step 3 | ✅ |
| 今日统计第二列改为间隔 | Task 1 + Task 2 + Task 3 Step 3 | ✅ |
| 间隔格式 XhXm 或 — | Task 2 Step 2 | ✅ |
| build 成功 | Task 4 | ✅ |
