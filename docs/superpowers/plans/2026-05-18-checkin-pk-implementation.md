# Checkin PK 功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将底部导航 Checkin 页面从 UI 存根改造为完整的打卡记录 + PK 对决页面，含 Supabase 同步。

**Architecture:** Room 本地缓存（`checkin_records` 表）+ Supabase REST API（`checkins` 表）双向同步；沿用现有 6s callbackFlow 轮询模式；ViewModel StateFlow → Compose UI。

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, OkHttp + Supabase REST, Coroutines/Flow

---

### Task 1: 数据模型 — CheckinData.kt

**Files:**
- Create: `app/src/main/java/com/standbyus/app/data/model/CheckinData.kt`

**Context:** 参考 `app/src/main/java/com/standbyus/app/data/model/UserStatus.kt` 的 `toMap()`/`fromMap()` 模式（手写 org.json 映射），用于 Supabase 序列化/反序列化。

- [ ] **Step 1: 创建 CheckinData.kt**

```kotlin
package com.standbyus.app.data.model

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
        private const val TAG = "CheckinData"

        fun fromMap(map: Map<String, Any>): CheckinData {
            return CheckinData(
                id = (map["id"] as? Number)?.toLong() ?: 0,
                userId = map["userId"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0,
                note = map["note"] as? String ?: ""
            )
        }
    }
}
```

- [ ] **Step 2: 验证文件正确编译**

Run: 等待最终 build 时验证

---

### Task 2: Room Entity — CheckinRecordEntity.kt

**Files:**
- Create: `app/src/main/java/com/standbyus/app/data/local/CheckinRecordEntity.kt`

**Context:** 参考 `app/src/main/java/com/standbyus/app/data/local/StatusEntity.kt` 模式。需要包含 entity ↔ data model 的转换函数。

- [ ] **Step 1: 创建 CheckinRecordEntity.kt**

```kotlin
package com.standbyus.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.standbyus.app.data.model.CheckinData

@Entity(tableName = "checkin_records")
data class CheckinRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Long,
    val note: String = ""
)

fun CheckinRecordEntity.toData(): CheckinData = CheckinData(
    id = id,
    userId = userId,
    timestamp = timestamp,
    note = note
)

fun CheckinData.toEntity(): CheckinRecordEntity = CheckinRecordEntity(
    userId = userId,
    timestamp = timestamp,
    note = note
)
```

---

### Task 3: Room DAO — CheckinDao.kt

**Files:**
- Create: `app/src/main/java/com/standbyus/app/data/local/CheckinDao.kt`

- [ ] **Step 1: 创建 CheckinDao.kt**

```kotlin
package com.standbyus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    /** 获取从 since 时间以来的所有活跃日期（以天为单位），用于计算连续打卡 */
    @Query("SELECT DISTINCT CAST(timestamp / 86400000 AS INTEGER) FROM checkin_records " +
           "WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getActiveDays(userId: String, since: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: CheckinRecordEntity)

    @Query("DELETE FROM checkin_records WHERE userId = :userId")
    suspend fun clearUserRecords(userId: String)

    /** 获取本地缓存的记录列表（供轮询 fallback 使用） */
    @Query("SELECT * FROM checkin_records WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getRecordsSince(userId: String, since: Long): List<CheckinRecordEntity>
}
```

---

### Task 4: AppDatabase 添加 checkin_records 表

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/data/local/AppDatabase.kt`

- [ ] **Step 1: 更新 AppDatabase.kt**

把 `AppDatabase` 从：

```kotlin
@Database(entities = [StatusEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
}
```

改为：

```kotlin
@Database(entities = [StatusEntity::class, CheckinRecordEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statusDao(): StatusDao
    abstract fun checkinDao(): CheckinDao
}
```

（版本号从 1 → 2，已有 `fallbackToDestructiveMigration()` 无需手动迁移）

---

### Task 5: AppModule 注册 CheckinDao

**Files:**
- Modify: `app/src/main/java/com/standbyus/app/di/AppModule.kt`

- [ ] **Step 1: 添加 CheckinDao 的 @Provides**

在 `AppModule` 中添加：

```kotlin
@Provides
fun provideCheckinDao(database: AppDatabase): CheckinDao = database.checkinDao()
```

---

### Task 6: CheckinRepository — 数据同步仓库

**Files:**
- Create: `app/src/main/java/com/standbyus/app/data/repository/CheckinRepository.kt`

**Context:** 严格复制 `app/src/main/java/com/standbyus/app/data/repository/StatusRepository.kt` 的模式：
- `submitCheckIn()` → Supabase `create()` + Room insert
- `observeCheckIns()` → callbackFlow 轮询（6s）
- Room 作为 fallback 缓存
- `widgetScope` 模式：`CoroutineScope(SupervisorJob() + Dispatchers.IO)`
- Supabase 表名：`"checkins"`
- `table"checkins"` 暂无 PostgreSQL 预建（代码首次运行时自动通过 Supabase API 创建失败 — 需要先手工建表或由首次 create 失败后处理）
  - **注意**：Supabase 需要预先创建 `checkins` 表。需要通过 Supabase dashboard 或 SQL 创建。本文档假设表已存在。

- [ ] **Step 1: 创建 CheckinRepository.kt**

```kotlin
package com.standbyus.app.data.repository

import android.content.Context
import android.util.Log
import com.standbyus.app.data.local.CheckinDao
import com.standbyus.app.data.local.toData
import com.standbyus.app.data.local.toEntity
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckinRepository @Inject constructor(
    private val supabaseService: SupabaseService,
    private val checkinDao: CheckinDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CheckinRepo"
        private const val TABLE = "checkins"
        private const val POLL_MS = 6_000L
    }

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 提交一条打卡记录 */
    suspend fun submitCheckIn(note: String = ""): Boolean {
        val userId = supabaseService.getCachedDeviceId()
        if (userId.isEmpty()) {
            Log.e(TAG, "submitCheckIn: no device ID")
            return false
        }
        val now = System.currentTimeMillis()
        val data = CheckinData(userId = userId, timestamp = now, note = note)
        return try {
            // ① 写入 Supabase
            supabaseService.create(TABLE, data.toMap())
            Log.d(TAG, "Supabase create success")
            // ② Room 缓存
            checkinDao.insert(data.toEntity())
            Log.d(TAG, "Room insert success")
            true
        } catch (e: Exception) {
            Log.e(TAG, "submitCheckIn failed: ${e.message}", e)
            false
        }
    }

    /** 获取今日打卡次数 */
    suspend fun getTodayCount(userId: String): Int {
        val startOfDay = getStartOfDay()
        return checkinDao.getTodayCount(userId, startOfDay)
    }

    /** 获取今日首次打卡时间 */
    suspend fun getFirstCheckinTime(userId: String): Long? {
        val startOfDay = getStartOfDay()
        return checkinDao.getFirstCheckinTime(userId, startOfDay)
    }

    /** 获取上次打卡时间 */
    suspend fun getLastCheckinTime(userId: String): Long? {
        return checkinDao.getLastCheckinTime(userId)
    }

    /** 获取本周打卡次数 */
    suspend fun getWeekCount(userId: String): Int {
        val startOfWeek = getStartOfWeek()
        return checkinDao.getWeekCount(userId, startOfWeek)
    }

    /** 获取本月打卡次数 */
    suspend fun getMonthCount(userId: String): Int {
        val startOfMonth = getStartOfMonth()
        return checkinDao.getMonthCount(userId, startOfMonth)
    }

    /** 轮询观察某用户的打卡记录 */
    fun observeCheckIns(userId: String, since: Long): Flow<List<CheckinData>> = callbackFlow {
        suspend fun fetchFromRemote() {
            try {
                val results = supabaseService.query(
                    TABLE,
                    "userId=eq.$userId&timestamp=gte.$since&order=timestamp.desc"
                )
                if (results.isNotEmpty()) {
                    val records = results.map { CheckinData.fromMap(it) }
                    // 缓存到 Room
                    records.forEach { checkinDao.insert(it.toEntity()) }
                    trySend(records)
                } else {
                    // 远程无数据，尝试本地缓存
                    val cached = checkinDao.getRecordsSince(userId, since)
                    if (cached.isNotEmpty()) trySend(cached.map { it.toData() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "observeCheckIns remote failed: ${e.message}")
                // fallback 到 Room 缓存
                val cached = checkinDao.getRecordsSince(userId, since)
                if (cached.isNotEmpty()) trySend(cached.map { it.toData() })
            }
        }

        // 首次立即执行
        fetchFromRemote()

        // 定时轮询
        val job = widgetScope.launch {
            delay(POLL_MS)
            while (isActive) {
                fetchFromRemote()
                delay(POLL_MS)
            }
        }
        awaitClose { job.cancel() }
    }

    /** 计算连续打卡天数 */
    suspend fun getStreakDays(userId: String): Int {
        val startOfWeek = getStartOfWeek()
        val activeDays = checkinDao.getActiveDays(userId, startOfWeek)
        if (activeDays.isEmpty()) return 0

        // 把活跃天的 epoch day 转为本地日期
        val todayEpochDay = System.currentTimeMillis() / 86400000
        val sorted = activeDays.sortedDescending()
        var streak = 0
        for (i in sorted.indices) {
            if (sorted[i] == todayEpochDay - i) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    // ── 时间工具 ──

    private fun getStartOfDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getActualMinimum(java.util.Calendar.DAY_OF_WEEK))
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
```

---

### Task 7: CheckinViewModel — UI 状态管理 + PK 计算

**Files:**
- Create: `app/src/main/java/com/standbyus/app/ui/checkin/CheckinViewModel.kt`

**Context:** 参考 `app/src/main/java/com/standbyus/app/ui/home/HomeViewModel.kt` 的 Hilt ViewModel 模式。需要：
- 注入 `CheckinRepository`、`SupabaseService`、`PairingRepository`、`ApplicationContext`
- 解析 partnerId（同 HomeViewModel：SharedPreferences 缓存 → PairingRepository）
- 计算 PK 统计数据
- 暴露 `StateFlow<CheckinUiState>`

- [ ] **Step 1: 创建 CheckinViewModel.kt**

```kotlin
package com.standbyus.app.ui.checkin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.CheckinRepository
import com.standbyus.app.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
    val subtitle: String = "保持冷静，继续拉",
    val dateDisplay: String = ""
)

enum class RiskLevel(val label: String, val emoji: String) {
    NORMAL("正常", "😊"),
    MILD("轻度", "🤔"),
    ATTENTION("注意", "😰")
}

data class PKStats(
    val myCount: Int,
    val partnerCount: Int
) {
    val totalCount: Int get() = myCount + partnerCount
    val myPercentage: Float get() = if (totalCount > 0) myCount.toFloat() / totalCount else 0.5f
    val partnerPercentage: Float get() = 1f - myPercentage
    val winner: String? get() = when {
        myCount > partnerCount -> "me"
        partnerCount > myCount -> "partner"
        else -> null
    }
    val leadAmount: Int get() = kotlin.math.abs(myCount - partnerCount)

    val daysUntilMonthEnd: Int get() {
        val now = Calendar.getInstance()
        val endOfMonth = Calendar.getInstance()
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        return (endOfMonth.timeInMillis - now.timeInMillis).let {
            kotlin.math.max(1, (it / 86400000L).toInt())
        }
    }

    val partnerCatchUpRate: Float get() {
        if (myCount <= partnerCount || daysUntilMonthEnd == 0) return 0f
        return (leadAmount.toFloat() / daysUntilMonthEnd).let {
            kotlin.math.round(it * 100) / 100f
        }
    }

    val myCatchUpRate: Float get() {
        if (partnerCount <= myCount || daysUntilMonthEnd == 0) return 0f
        return (leadAmount.toFloat() / daysUntilMonthEnd).let {
            kotlin.math.round(it * 100) / 100f
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CheckinViewModel @Inject constructor(
    private val checkinRepository: CheckinRepository,
    private val pairingRepository: PairingRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "CheckinVM"
        private const val PREFS_NAME = "pairing"
        private const val KEY_PARTNER_ID = "partner_id"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _myUserId = MutableStateFlow("")
    private val _partnerUserId = MutableStateFlow("")
    private val _isCheckingIn = MutableStateFlow(false)

    /** 本月起始时间戳 */
    private val monthStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    /** 今日起始时间戳 */
    private val dayStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    /** 本周起始（周一 00:00） */
    private val weekStart: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    val uiState: StateFlow<CheckinUiState> = combine(
        _myUserId,
        _partnerUserId,
        _isCheckingIn
    ) { myId, partnerId, checkingIn ->
        if (myId.isEmpty()) return@combine CheckinUiState(
            subtitle = "保持冷静，继续拉",
            dateDisplay = formatDateDisplay()
        )

        val todayCount = checkinRepository.getTodayCount(myId)
        val firstCheckinTime = checkinRepository.getFirstCheckinTime(myId)
        val lastCheckinTime = checkinRepository.getLastCheckinTime(myId)
        val weekTotal = checkinRepository.getWeekCount(myId)
        val streak = checkinRepository.getStreakDays(myId)
        val weekAverage = calculateWeekAverage(weekTotal)

        // 计算便秘风险
        val riskLevel = calculateRiskLevel(lastCheckinTime)

        // PK 统计
        val pkStats = if (partnerId.isNotEmpty()) {
            val myMonthCount = checkinRepository.getMonthCount(myId)
            val partnerMonthCount = checkinRepository.getMonthCount(partnerId)
            PKStats(myMonthCount, partnerMonthCount)
        } else null

        CheckinUiState(
            todayCount = todayCount,
            firstCheckinTime = if (firstCheckinTime != null) formatTime(firstCheckinTime) else "—",
            riskLevel = riskLevel,
            weeklyTotal = weekTotal,
            weeklyAverage = weekAverage,
            streak = streak,
            isCheckingIn = checkingIn,
            isPaired = partnerId.isNotEmpty(),
            pkStats = pkStats,
            subtitle = "保持冷静，继续拉",
            dateDisplay = formatDateDisplay()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CheckinUiState())

    init {
        val uid = supabaseService.getCachedDeviceId()
        Log.d(TAG, "deviceId=$uid")
        _myUserId.value = uid

        // 从缓存读取 partner ID
        val cachedPartnerId = prefs.getString(KEY_PARTNER_ID, null)
        if (!cachedPartnerId.isNullOrEmpty()) {
            Log.d(TAG, "partner from cache: $cachedPartnerId")
            _partnerUserId.value = cachedPartnerId
        } else {
            // 联网查询
            viewModelScope.launch {
                try {
                    val pair = pairingRepository.findPairByUserId(uid)
                    if (pair != null) {
                        val partnerId = when {
                            pair.user1Id == uid && pair.user2Id.isNotEmpty() -> pair.user2Id
                            pair.user2Id == uid && pair.user1Id.isNotEmpty() -> pair.user1Id
                            else -> ""
                        }
                        if (partnerId.isNotEmpty()) {
                            prefs.edit().putString(KEY_PARTNER_ID, partnerId).apply()
                            _partnerUserId.value = partnerId
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "pair lookup failed", e)
                }
            }
        }

        // 启动轮询（自己 + 伴侣的打卡记录）
        viewModelScope.launch {
            _myUserId.collect { id ->
                if (id.isNotEmpty()) {
                    checkinRepository.observeCheckIns(id, monthStart)
                        .collect { /* state flow 自动重新计算 */ }
                }
            }
        }

        viewModelScope.launch {
            _partnerUserId.collect { id ->
                if (id.isNotEmpty()) {
                    checkinRepository.observeCheckIns(id, monthStart)
                        .collect { /* state flow 自动重新计算 */ }
                }
            }
        }
    }

    fun checkIn() {
        if (_isCheckingIn.value) return
        _isCheckingIn.value = true
        viewModelScope.launch {
            try {
                checkinRepository.submitCheckIn()
            } catch (e: Exception) {
                Log.e(TAG, "checkIn failed", e)
            } finally {
                _isCheckingIn.value = false
            }
        }
    }

    private fun calculateRiskLevel(lastCheckinTime: Long?): RiskLevel {
        if (lastCheckinTime == null) return RiskLevel.ATTENTION
        val hoursSince = (System.currentTimeMillis() - lastCheckinTime) / 3_600_000
        return when {
            hoursSince <= 24 -> RiskLevel.NORMAL
            hoursSince <= 48 -> RiskLevel.MILD
            else -> RiskLevel.ATTENTION
        }
    }

    private fun calculateWeekAverage(weekTotal: Int): Float {
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        // Monday=2, Sunday=1 → days passed = dayOfWeek - 1
        val daysPassed = when (dayOfWeek) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
        if (daysPassed == 0) return 0f
        return kotlin.math.round(weekTotal.toFloat() / daysPassed * 10) / 10f
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatDateDisplay(): String {
        val sdf = SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINESE)
        return "📅 ${sdf.format(Date())}"
    }
}
```

---

### Task 8: CheckinScreen 重写 — 完整 UI

**Files:**
- Rewrite: `app/src/main/java/com/standbyus/app/ui/checkin/CheckinScreen.kt`

**Context:** 完全重写。新布局：
1. AppHeader("拉了么") — 无 onBack（底部导航标签页）
2. 副标题 + 日期
3. 今日统计 3 列卡片（已记录 / 时间 / 便秘风险）
4. 本周统计 3 列卡片（总共 / 平均 / 连续打卡）
5. PK 对决区域
6. 固定底部大按钮（非滚动区域）

- [ ] **Step 1: 重写 CheckinScreen.kt**

```kotlin
package com.standbyus.app.ui.checkin

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.standbyus.app.ui.components.AppHeader

// ── Design Tokens ──
private val BgColor = Color(0xFFFFF8F5)
private val SurfaceColor = Color(0xFFFFFFFF)
private val PrimaryColor = Color(0xFFFFB4A2)
private val GradientEnd = Color(0xFFFF9E8E)
private val TextPrimary = Color(0xFF5A4A42)
private val TextSecondary = Color(0xFF9E8E86)
private val BorderColor = Color(0xFFF0EAE6)
private val WinAccent = Color(0xFFE8B84B)
private val DangerAccent = Color(0xFFE07070)

// ── Main Screen ──

@Composable
fun CheckinScreen(
    onBack: () -> Unit,  // 保留参数签名，MainActivity 中传空 lambda 或保留 popBackStack
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── App Bar ──
            AppHeader(title = "拉了么")

            // ── Scrollable content (takes remaining space) ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = state.subtitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFF09B6D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.dateDisplay,
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ── Today Stats Card ──
                ThreeColCard(
                    col1 = { StatCell("💩", "${state.todayCount} 次", "已记录", PrimaryColor.copy(alpha = 0.15f)) },
                    col2 = { StatCell("⏰", state.firstCheckinTime, "时间", Color(0xFFE3F2FD)) },
                    col3 = { StatCell(state.riskLevel.emoji, state.riskLevel.label, "便秘风险", Color(0xFFFFF8E1)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── This Week Stats Card ──
                ThreeColCard(
                    col1 = { StatCell("📊", "${state.weeklyTotal} 次", "总共", Color(0xFFE3F2FD)) },
                    col2 = { StatCell("📈", "${state.weeklyAverage}/天", "平均", Color(0xFFE8F5E9)) },
                    col3 = { StatCell("🔥", "${state.streak} 天", "连续打卡", Color(0xFFFFF3E0)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── PK Section ──
                if (state.isPaired && state.pkStats != null) {
                    PKCard(stats = state.pkStats!!)
                } else {
                    EmptyPKCard()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Big Checkin Button (fixed, not scrollable) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                BigCheckinButton(
                    onClick = { viewModel.checkIn() },
                    isCheckingIn = state.isCheckingIn
                )
            }

            // Space for bottom nav
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Three Column Card ──

@Composable
private fun ThreeColCard(
    col1: @Composable () -> Unit,
    col2: @Composable () -> Unit,
    col3: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceColor)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        col1()
        VerticalDivider()
        col2()
        VerticalDivider()
        col3()
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(BorderColor)
    )
}

@Composable
private fun StatCell(
    emoji: String,
    value: String,
    label: String,
    circleBg: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.weight(1f)
    ) {
        // Emoji in circle background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(circleBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

// ── PK Card ──

@Composable
private fun PKCard(stats: PKStats) {
    val isMeWinning = stats.winner == "me"
    val isTie = stats.winner == null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        Text(
            text = "🏆 本月拉屎大王对决",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // VS Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // My side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isMeWinning) {
                    Text(text = "👑", fontSize = 24.sp)
                }
                Text(
                    text = "我",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMeWinning) WinAccent else TextPrimary
                )
                Text(
                    text = "${stats.myCount} 次",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMeWinning) WinAccent else TextPrimary
                )
            }

            Text(
                text = if (isTie) "🤝" else "VS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            // Partner side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isMeWinning && !isTie) {
                    Text(text = "👑", fontSize = 24.sp)
                }
                Text(
                    text = "TA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!isMeWinning && !isTie) WinAccent else TextPrimary
                )
                Text(
                    text = "${stats.partnerCount} 次",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isMeWinning && !isTie) WinAccent else TextPrimary
                )
            }
        }

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF0EAE6))
        ) {
            // My portion (left side)
            Box(
                modifier = Modifier
                    .fillMaxWidth(stats.myPercentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isMeWinning) WinAccent else PrimaryColor)
            )
        }

        // Stats detail
        Text(
            text = when {
                isTie -> "🤝 不分胜负！势均力敌！"
                isMeWinning -> "🎉 领先 ${stats.leadAmount} 次！"
                else -> "💪 落后 ${stats.leadAmount} 次，继续努力！"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )

        if (!isTie) {
            Text(
                text = if (isMeWinning) {
                    "距离月底还有 ${stats.daysUntilMonthEnd} 天，TA 每天需多拉 ${stats.partnerCatchUpRate} 次才能追"
                } else {
                    "距离月底还有 ${stats.daysUntilMonthEnd} 天，每天多拉 ${stats.myCatchUpRate} 次就能反超！"
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyPKCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0F5A4A42),
                spotColor = Color(0x0F5A4A42)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🏆 本月拉屎大王对决",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "绑定伴侣后即可与 TA PK",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

// ── Big Checkin Button ──

@Composable
private fun BigCheckinButton(
    onClick: () -> Unit,
    isCheckingIn: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")

    // 3 圈水波纹，每个偏移 1s
    val rippleProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple1"
    )
    val rippleProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, 1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple2"
    )
    val rippleProgress3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, 2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple3"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .drawBehind {
                // 3 圈水波纹
                val rippleAlpha = (1f - rippleProgress1) * 0.4f
                val rippleScale = 1f + rippleProgress1 * 0.5f
                drawCircle(
                    color = PrimaryColor.copy(alpha = rippleAlpha),
                    radius = size.minDimension / 2f * rippleScale,
                    style = Stroke(width = 3.dp.toPx())
                )
                val rippleAlpha2 = (1f - rippleProgress2) * 0.4f
                val rippleScale2 = 1f + rippleProgress2 * 0.5f
                drawCircle(
                    color = PrimaryColor.copy(alpha = rippleAlpha2),
                    radius = size.minDimension / 2f * rippleScale2,
                    style = Stroke(width = 3.dp.toPx())
                )
                val rippleAlpha3 = (1f - rippleProgress3) * 0.4f
                val rippleScale3 = 1f + rippleProgress3 * 0.5f
                drawCircle(
                    color = PrimaryColor.copy(alpha = rippleAlpha3),
                    radius = size.minDimension / 2f * rippleScale3,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = Color(0x3F5A4A42),
                spotColor = Color(0x3F5A4A42)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(PrimaryColor, GradientEnd)
                )
            )
            .clickable(enabled = !isCheckingIn) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "💩", fontSize = 56.sp)
            Text(
                text = if (isCheckingIn) "记录中..." else "今天也顺利啦 🎉",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
```

---

### Task 9: 验证 Build

- [ ] **Step 1: Build 检查编译错误**

Run: `./gradlew assembleDebug`

预期：BUILD SUCCESSFUL。如果有编译错误，根据错误信息修复。

---

### Task 10: 创建 Supabase `checkins` 表

- [ ] **Step 1: Supabase SQL 建表**

通过 Supabase Dashboard → SQL Editor 执行：

```sql
CREATE TABLE IF NOT EXISTS checkins (
  id BIGSERIAL PRIMARY KEY,
  "userId" TEXT NOT NULL DEFAULT '',
  "timestamp" BIGINT NOT NULL DEFAULT 0,
  "note" TEXT NOT NULL DEFAULT ''
);
```

（注意：Supabase REST API 会自动将 camelCase 字段名转为小写，但 PostgreSQL 的带引号列名会保留大小写。实际测试确认 Supabase 如何处理——如果表创建后 API 返回列名为 `userid` 则调整查询参数。）

---

### 自审清单

- [ ] **Spec 覆盖**: 设计文档中所有功能（打卡记录、今日统计、本周统计、PK 对决、便秘风险、连续打卡、Supabase 同步）都有对应 task
- [ ] **无占位符**: 所有代码块包含完整实现
- [ ] **类型一致性**: `CheckinData` / `CheckinRecordEntity` / `CheckinDao` 之间字段名和类型一致（userId, timestamp, note）
