# 配对优化：用户名 + 对方备注 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在配对流程中让双方设置自己的用户名，配对后在设置页可编辑给对方的备注，首页展示对方名字代替硬编码"她"

**Architecture:** 用户名存 Supabase `pairs` 表（`user1Name`/`user2Name`），备注存本地 `pairing` SharedPreferences。仅修改 Repository/ViewModel/UI 层，不碰 Room/Widget。

**Tech Stack:** Kotlin + Jetpack Compose + Hilt + Supabase REST API

---

### 前置准备：Supabase 表迁移

在 Supabase 控制台 SQL 编辑器执行：
```sql
ALTER TABLE pairs ADD COLUMN IF NOT EXISTS "user1Name" TEXT NOT NULL DEFAULT '';
ALTER TABLE pairs ADD COLUMN IF NOT EXISTS "user2Name" TEXT NOT NULL DEFAULT '';
```

---

### Task 1: PairingRepository — 新增名字参数

**Files:** Modify `data/repository/PairingRepository.kt`

- [ ] **Step 1.1: 修改 `createPairingCode` 签名**

```kotlin
suspend fun createPairingCode(creatorUserId: String, creatorName: String): String {
```

- [ ] **Step 1.2: 修改 `createPairingCode` 中 data map**

```kotlin
val data = mapOf(
    "pairId" to pairId,
    "user1Id" to creatorUserId,
    "user2Id" to "",
    "user1Name" to creatorName,
    "user2Name" to ""
)
```

- [ ] **Step 1.3: 修改 `joinPair` 签名，新增 `joinerName` 参数**

```kotlin
suspend fun joinPair(pairId: String, userId: String, joinerName: String): JoinResult {
```

- [ ] **Step 1.4: 修改 `joinPair` 中 PATCH 逻辑，写入 `user2Name`**

当加入者为 user2 时：
```kotlin
supabaseService.update(TABLE, "pairId=eq.$pairId", mapOf("user2Id" to userId, "user2Name" to joinerName))
```

当加入者为 user1 时（空位）：
```kotlin
supabaseService.update(TABLE, "pairId=eq.$pairId", mapOf("user1Id" to userId, "user1Name" to joinerName))
```

### Task 2: SettingsViewModel — 新增名字相关状态

**Files:** Modify `ui/settings/SettingsViewModel.kt`

- [ ] **Step 2.1: 新增 StateFlow**

```kotlin
private val _nameInput = MutableStateFlow("")
val nameInput: StateFlow<String> = _nameInput.asStateFlow()

private val _nicknameInput = MutableStateFlow("")
val nicknameInput: StateFlow<String> = _nicknameInput.asStateFlow()

// 配对成功后从缓存读取备注
private val _partnerNickname = MutableStateFlow("")
val partnerNickname: StateFlow<String> = _partnerNickname.asStateFlow()
```

- [ ] **Step 2.2: 新增 updateNameInput / updateNickname 方法**

```kotlin
fun updateNameInput(name: String) { _nameInput.value = name }

fun updateNickname(nickname: String) {
    _partnerNickname.value = nickname
    prefs.edit().putString("partner_nickname", nickname).apply()
}
```

- [ ] **Step 2.3: 修改 `createCode()` 传入 creatorName**

```kotlin
fun createCode() {
    viewModelScope.launch {
        _status.value = "生成配对码中…"
        try {
            val uid = supabaseService.getCachedDeviceId()
            val myName = _nameInput.value.trim().ifEmpty { "我" }
            val code = pairingRepository.createPairingCode(uid, myName)
            _pairingCode.value = code
            _status.value = "配对码: $code，等待对方连接…"
            // 缓存自己的名字
            prefs.edit().putString("self_name", myName).apply()
            pollPairingComplete(code)
        } catch (e: Exception) {
            _status.value = "❌ 生成失败：${e.localizedMessage}"
        }
    }
}
```

- [ ] **Step 2.4: 修改 `joinPair()` 传入 joinerName**

```kotlin
fun joinPair() {
    viewModelScope.launch {
        _status.value = "正在连接…"
        try {
            val uid = supabaseService.getCachedDeviceId()
            val myName = _nameInput.value.trim().ifEmpty { "我" }
            val pairId = _joinCodeInput.value
            // 先查一次获取对方信息
            val pairInfo = pairingRepository.getPairInfo(pairId)
            if (pairInfo == null) {
                _status.value = "❌ 配对码无效"
                return@launch
            }
            val result = pairingRepository.joinPair(pairId, uid, myName)
            if (result.success) {
                _isPaired.value = true
                _justPaired.value = true
                _status.value = "✅ 配对成功！"
                val partnerName = pairInfo.user1Name.ifEmpty { "" }
                prefs.edit().apply {
                    putBoolean("is_paired", true)
                    putString("partner_id", result.partnerId)
                    putString("self_name", myName)
                    putString("partner_name", partnerName)
                    apply()
                }
            } else {
                _status.value = "❌ 配对失败，请检查配对码"
            }
        } catch (e: Exception) {
            _status.value = "❌ 连接失败：${e.localizedMessage}"
        }
    }
}
```

- [ ] **Step 2.5: 修改 `pollPairingComplete()` 缓存 partner_name**

```kotlin
private suspend fun pollPairingComplete(code: String) {
    val uid = supabaseService.getCachedDeviceId()
    repeat(30) {
        delay(3000)
        try {
            val pair = pairingRepository.getPairInfo(code) ?: return@repeat
            val partnerAssigned = when {
                pair.user1Id == uid -> pair.user2Id.isNotEmpty()
                pair.user2Id == uid -> pair.user1Id.isNotEmpty()
                else -> pair.user1Id.isNotEmpty() && pair.user2Id.isNotEmpty()
            }
            if (partnerAssigned) {
                _isPaired.value = true
                _justPaired.value = true
                _status.value = "✅ 配对成功！"
                cacheAll(pair, uid)
                // 缓存对方用户名
                val partnerName = when {
                    pair.user1Id == uid -> pair.user2Name
                    pair.user2Id == uid -> pair.user1Name
                    else -> ""
                }
                prefs.edit().putString("partner_name", partnerName).apply()
                return
            }
        } catch (_: Exception) { }
    }
}
```

- [ ] **Step 2.6: 修改 `checkExistingPair()` 读取已缓存的备注**

```kotlin
init {
    checkExistingPair()
    _nicknameInput.value = prefs.getString("partner_nickname", "") ?: ""
}
```

- [ ] **Step 2.7: 修改 `unpair()` 清除所有字段**

`unpair()` 已调用 `prefs.edit().clear().apply()`，覆盖所有新增字段，无需额外操作。

### Task 3: SettingsScreen UI — 名字输入框 + 备注编辑

**Files:** Modify `ui/settings/SettingsScreen.kt`

- [ ] **Step 3.1: 在未配对区域添加名字输入框（创建侧）**

在"生成配对码"按钮上方增加：
```kotlin
// 创建配对码 - 名字输入
OutlinedTextField(
    value = viewModel.nameInput.collectAsState().value,
    onValueChange = { viewModel.updateNameInput(it) },
    placeholder = { Text("输入你的昵称", color = TextSecondary.copy(alpha = 0.5f)) },
    singleLine = true,
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = Border,
        cursorColor = Primary
    ),
    modifier = Modifier.fillMaxWidth()
)
Spacer(modifier = Modifier.height(12.dp))
```

按钮启用条件改为 `nameInput` 非空：
```kotlin
Button(
    onClick = { viewModel.createCode() },
    enabled = nameInput.isNotBlank(),
    ...
)
```

- [ ] **Step 3.2: 在加入配对区域添加名字输入框（加入侧）**

在"输入对方的配对码"上方增加：
```kotlin
Text(
    text = "输入你的昵称",
    fontSize = 13.sp,
    color = TextSecondary
)
Spacer(modifier = Modifier.height(12.dp))
OutlinedTextField(
    value = viewModel.nameInput.collectAsState().value,
    onValueChange = { viewModel.updateNameInput(it) },
    placeholder = { Text("输入你的昵称", color = TextSecondary.copy(alpha = 0.5f)) },
    singleLine = true,
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = Border,
        cursorColor = Primary
    ),
    modifier = Modifier.fillMaxWidth()
)
Spacer(modifier = Modifier.height(12.dp))
```

"连接"按钮启用条件改为同时 nameInput 非空：
```kotlin
Button(
    onClick = { viewModel.joinPair() },
    enabled = joinCodeInput.length == 6 && nameInput.isNotBlank(),
    ...
)
```

- [ ] **Step 3.3: 已配对区域增加对方备注输入框**

在"已绑定"文字下方、"断开连接"按钮上方增加：
```kotlin
Spacer(modifier = Modifier.height(12.dp))
Text(
    text = "对方昵称",
    fontSize = 13.sp,
    color = TextSecondary
)
Spacer(modifier = Modifier.height(8.dp))
val nickname by viewModel.nicknameInput.collectAsState()
OutlinedTextField(
    value = nickname,
    onValueChange = { viewModel.updateNickname(it) },
    placeholder = { Text("输入对方昵称", color = TextSecondary.copy(alpha = 0.5f)) },
    singleLine = true,
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Primary,
        unfocusedBorderColor = Border,
        cursorColor = Primary
    ),
    modifier = Modifier.fillMaxWidth()
)
```

### Task 4: HomeViewModel — 暴露对方显示名

**Files:** Modify `ui/home/HomeViewModel.kt`

- [ ] **Step 4.1: 新增 `partnerDisplayName` StateFlow**

```kotlin
private val _partnerDisplayName = MutableStateFlow("对方")
val partnerDisplayName: StateFlow<String> = _partnerDisplayName.asStateFlow()
```

- [ ] **Step 4.2: init 中根据 partner_id 缓存获取 partner_name 和 partner_nickname**

```kotlin
// 在 init 的 partner ID 解析后：
val cachedNickname = prefs.getString("partner_nickname", null)
val cachedPartnerName = prefs.getString("partner_name", null)
_partnerDisplayName.value = when {
    !cachedNickname.isNullOrEmpty() -> cachedNickname
    !cachedPartnerName.isNullOrEmpty() -> cachedPartnerName
    else -> "对方"
}
```

- [ ] **Step 4.3: 联网查询到配对后也更新 partnerDisplayName**

在 `findPairByUserId` 成功的分支中：
```kotlin
if (pair != null) {
    // ... existing partnerId resolution
    // Also update display name
    val partnerName = when {
        pair.user1Id == uid -> pair.user2Name
        pair.user2Id == uid -> pair.user1Name
        else -> ""
    }
    val cachedNickname = prefs.getString("partner_nickname", null)
    _partnerDisplayName.value = when {
        !cachedNickname.isNullOrEmpty() -> cachedNickname
        !partnerName.isNullOrEmpty() -> partnerName
        else -> "对方"
    }
}
```

### Task 5: HomeScreen — 显示对方名字

**Files:** Modify `ui/home/HomeScreen.kt`

- [ ] **Step 5.1: 收集 partnerDisplayName**

```kotlin
val partnerStatus by viewModel.partnerStatus.collectAsState()
val myStatus by viewModel.myStatus.collectAsState()
val partnerDisplayName by viewModel.partnerDisplayName.collectAsState()
```

- [ ] **Step 5.2: 传入 PartnerStatusCard**

```kotlin
PartnerStatusCard(
    status = partnerStatus!!,
    userName = partnerDisplayName,
    modifier = ...
)
```

- [ ] **Step 5.3: 修改 `PartnerStatusCard` 签名增加 userName 参数**

```kotlin
@Composable
private fun PartnerStatusCard(
    status: UserStatus,
    userName: String,
    modifier: Modifier = Modifier
) {
```

- [ ] **Step 5.4: 替换硬编码"她"为 userName**

原代码第 246 行：
```kotlin
text = "她刚刚更新了这个心情 · ${formatRelativeTime(status.updatedAt)}",
```
改为：
```kotlin
text = "$userName刚刚更新了这个心情 · ${formatRelativeTime(status.updatedAt)}",
```

也把第 253 行 "需要我哄哄她吗？" 改为：
```kotlin
text = "需要我哄哄$userName吗？",
```
