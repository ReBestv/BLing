package com.standbyus.app.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.PairingInfo
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.remote.ThemeRepository
import com.standbyus.app.data.repository.AvatarRepository
import com.standbyus.app.data.repository.PairingCleanupRepository
import com.standbyus.app.data.repository.PairingRepository
import com.standbyus.app.data.repository.StatusRepository
import com.standbyus.app.ui.theme.EmojiThemeManager
import com.standbyus.app.ui.theme.EmojiThemeSet
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val pairingCleanupRepository: PairingCleanupRepository,
    private val avatarRepository: AvatarRepository,
    private val statusRepository: StatusRepository,
    private val supabaseService: SupabaseService,
    private val themeRepository: ThemeRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _pairingCode = MutableStateFlow("")
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _joinCodeInput = MutableStateFlow("")
    val joinCodeInput: StateFlow<String> = _joinCodeInput.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _isPaired = MutableStateFlow(false)
    val isPaired: StateFlow<Boolean> = _isPaired.asStateFlow()

    private val _currentTheme = MutableStateFlow(EmojiThemeManager.getCurrentTheme(context))
    val currentTheme: StateFlow<EmojiThemeSet> = _currentTheme.asStateFlow()

    private val _availableThemes = MutableStateFlow(listOf(EmojiThemeManager.defaultTheme))
    val availableThemes: StateFlow<List<EmojiThemeSet>> = _availableThemes.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _nameInput = MutableStateFlow("")
    val nameInput: StateFlow<String> = _nameInput.asStateFlow()

    private val _nicknameInput = MutableStateFlow("")
    val nicknameInput: StateFlow<String> = _nicknameInput.asStateFlow()

    private val _partnerNickname = MutableStateFlow("")
    val partnerNickname: StateFlow<String> = _partnerNickname.asStateFlow()

    private val _partnerDisplayName = MutableStateFlow("对方")
    val partnerDisplayName: StateFlow<String> = _partnerDisplayName.asStateFlow()

    private val _avatarEmoji = MutableStateFlow("🙂")
    val avatarEmoji: StateFlow<String> = _avatarEmoji.asStateFlow()

    private val _avatarUrl = MutableStateFlow("")
    val avatarUrl: StateFlow<String> = _avatarUrl.asStateFlow()

    private val _avatarUploading = MutableStateFlow(false)
    val avatarUploading: StateFlow<Boolean> = _avatarUploading.asStateFlow()

    private val _myName = MutableStateFlow("")
    val myName: StateFlow<String> = _myName.asStateFlow()

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)

    init {
        _avatarEmoji.value = prefs.getString("avatar_emoji", "🙂") ?: "🙂"
        _avatarUrl.value = prefs.getString("avatar_url", "") ?: ""
        _myName.value = prefs.getString("self_name", "") ?: ""
        _nicknameInput.value = prefs.getString("partner_nickname", "") ?: ""
        updatePartnerDisplayName()
        checkExistingPair()
        loadThemes()
    }

    fun selectAvatar(emoji: String) {
        saveAvatar(emoji = emoji, avatarUrl = "")
        syncAvatarSnapshot()
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _avatarUploading.value = true
            _status.value = "正在上传头像..."
            try {
                val uid = cachedOrNewDeviceId()
                val uploadedUrl = avatarRepository.uploadAvatar(uri, uid)
                saveAvatar(
                    emoji = _avatarEmoji.value.ifEmpty { DEFAULT_AVATAR },
                    avatarUrl = uploadedUrl
                )
                syncAvatarSnapshot()
                _status.value = "头像已更新"
            } catch (e: Exception) {
                Log.e(TAG, "upload avatar failed", e)
                _status.value = "头像上传失败：${e.localizedMessage ?: "请稍后重试"}"
            } finally {
                _avatarUploading.value = false
            }
        }
    }

    private fun loadThemes() {
        viewModelScope.launch {
            val manifest = themeRepository.fetchManifest(context)
            if (manifest != null) {
                EmojiThemeManager.updateThemes(manifest.themes)
            }
            _availableThemes.value = EmojiThemeManager.themes
            _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
        }
    }

    private fun checkExistingPair() {
        if (prefs.getBoolean("is_paired", false)) {
            _isPaired.value = true
        }
        viewModelScope.launch {
            val uid = supabaseService.getCachedDeviceId()
            val pair = runCatching { pairingRepository.findPairByUserId(uid) }.getOrNull()
            val partnerAssigned = pair?.partnerIdFor(uid).orEmpty().isNotEmpty()
            if (pair != null && partnerAssigned) {
                _isPaired.value = true
                cacheAll(pair, uid)
            } else {
                _isPaired.value = false
                clearPairingCache()
                updatePartnerDisplayName()
            }
            _loading.value = false
        }
    }

    private fun cacheAll(pair: PairingInfo, myUid: String) {
        val partnerId = pair.partnerIdFor(myUid)
        val partnerName = when {
            pair.user1Id == myUid -> pair.user2Name
            pair.user2Id == myUid -> pair.user1Name
            else -> ""
        }
        prefs.edit().apply {
            putBoolean("is_paired", true)
            putString("partner_id", partnerId)
            putString("partner_name", partnerName)
            putString("pair_id", pair.pairId)
            apply()
        }
        updatePartnerDisplayName(partnerName = partnerName)
    }

    fun createCode() {
        viewModelScope.launch {
            _status.value = "生成配对码中..."
            try {
                val uid = supabaseService.getCachedDeviceId()
                val myName = _nameInput.value.trim().ifEmpty { "我" }
                val code = pairingRepository.createPairingCode(uid, myName)
                _pairingCode.value = code
                _status.value = "配对码：$code，等待对方连接..."
                prefs.edit().putString("self_name", myName).apply()
                _myName.value = myName
                pollPairingComplete(code)
            } catch (e: Exception) {
                _status.value = "生成失败：${e.localizedMessage ?: "请稍后重试"}"
            }
        }
    }

    private suspend fun pollPairingComplete(code: String) {
        val uid = supabaseService.getCachedDeviceId()
        repeat(30) {
            delay(3000)
            try {
                val pair = pairingRepository.getPairInfo(code) ?: return@repeat
                val partnerAssigned = pair.partnerIdFor(uid).isNotEmpty()
                if (partnerAssigned) {
                    _isPaired.value = true
                    _status.value = "配对成功"
                    cacheAll(pair, uid)
                    val partnerName = when {
                        pair.user1Id == uid -> pair.user2Name
                        pair.user2Id == uid -> pair.user1Name
                        else -> ""
                    }
                    prefs.edit().putString("partner_name", partnerName).apply()
                    prefs.edit().putString("avatar_emoji", _avatarEmoji.value).apply()
                    updatePartnerDisplayName(partnerName = partnerName)
                    return
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateJoinCode(code: String) {
        _joinCodeInput.value = code
    }

    fun joinPair() {
        viewModelScope.launch {
            _status.value = "正在连接..."
            try {
                val uid = supabaseService.getCachedDeviceId()
                val myName = _nameInput.value.trim().ifEmpty { "我" }
                val pairId = _joinCodeInput.value
                val pairInfo = pairingRepository.getPairInfo(pairId)
                if (pairInfo == null) {
                    _status.value = "配对码无效"
                    return@launch
                }
                val result = pairingRepository.joinPair(pairId, uid, myName)
                if (result.success) {
                    _isPaired.value = true
                    _status.value = "配对成功"
                    val partnerName = pairInfo.user1Name.ifEmpty { "" }
                    prefs.edit().apply {
                        putBoolean("is_paired", true)
                        putString("partner_id", result.partnerId)
                        putString("self_name", myName)
                        putString("partner_name", partnerName)
                        putString("pair_id", pairInfo.pairId)
                        apply()
                    }
                    _myName.value = myName
                    updatePartnerDisplayName(partnerName = partnerName)
                } else {
                    _status.value = "配对失败，请检查配对码"
                }
            } catch (e: Exception) {
                _status.value = "连接失败：${e.localizedMessage ?: "请稍后重试"}"
            }
        }
    }

    fun unpair() {
        viewModelScope.launch {
            val hadFailures = runCatching {
                val uid = supabaseService.getCachedDeviceId()
                pairingCleanupRepository.unpairAndClearData(uid).hadFailures
            }.getOrDefault(true)
            prefs.edit().clear().apply()
            _avatarEmoji.value = "🙂"
            _avatarUrl.value = ""
            _isPaired.value = false
            _pairingCode.value = ""
            _joinCodeInput.value = ""
            _partnerNickname.value = ""
            _nicknameInput.value = ""
            _myName.value = ""
            _status.value = if (hadFailures) "已解除配对，旧记录未完全清理" else "已解除配对"
            _partnerDisplayName.value = "对方"
        }
    }

    fun selectTheme(themeId: String) {
        EmojiThemeManager.setCurrentTheme(context, themeId)
        _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
    }

    fun updateNameInput(name: String) {
        _nameInput.value = name
    }

    fun updateNickname(nickname: String) {
        _nicknameInput.value = nickname
        _partnerNickname.value = nickname
        prefs.edit().putString("partner_nickname", nickname).apply()
        updatePartnerDisplayName(nickname = nickname)
    }

    private fun updatePartnerDisplayName(
        nickname: String? = prefs.getString("partner_nickname", ""),
        partnerName: String? = prefs.getString("partner_name", "")
    ) {
        _partnerDisplayName.value = SettingsDisplayName.resolvePartnerDisplayName(
            nickname = nickname,
            partnerName = partnerName
        )
    }

    private fun clearPairingCache() {
        prefs.edit().apply {
            remove("is_paired")
            remove("partner_id")
            remove("partner_name")
            remove("pair_id")
            remove("partner_nickname")
            apply()
        }
    }

    private fun saveAvatar(emoji: String, avatarUrl: String) {
        val normalizedEmoji = emoji.ifEmpty { DEFAULT_AVATAR }
        _avatarEmoji.value = normalizedEmoji
        _avatarUrl.value = avatarUrl
        prefs.edit().apply {
            putString("avatar_emoji", normalizedEmoji)
            putString("avatar_url", avatarUrl)
            apply()
        }
    }

    private fun syncAvatarSnapshot() {
        viewModelScope.launch {
            runCatching {
                statusRepository.updateAvatarSnapshot(
                    userId = cachedOrNewDeviceId(),
                    avatarEmoji = _avatarEmoji.value.ifEmpty { DEFAULT_AVATAR },
                    avatarUrl = _avatarUrl.value
                )
            }.onFailure {
                Log.e(TAG, "sync avatar snapshot failed", it)
            }
        }
    }

    private fun cachedOrNewDeviceId(): String {
        return supabaseService.getCachedDeviceId().ifEmpty {
            supabaseService.getDeviceId(context)
        }
    }

    private fun PairingInfo.partnerIdFor(myUid: String): String {
        return when {
            user1Id == myUid -> user2Id
            user2Id == myUid -> user1Id
            else -> ""
        }
    }

    companion object {
        private const val TAG = "StandBySettingsVM"
        private const val DEFAULT_AVATAR = "\uD83D\uDE42"
    }
}
