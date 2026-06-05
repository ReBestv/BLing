package com.standbyus.app.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.PairingInfo
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.remote.ThemeRepository
import com.standbyus.app.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.standbyus.app.ui.theme.EmojiThemeSet
import com.standbyus.app.ui.theme.EmojiThemeManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
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

    private val _justPaired = MutableStateFlow(false)
    val justPaired: StateFlow<Boolean> = _justPaired.asStateFlow()

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

    private val _avatarEmoji = MutableStateFlow("🐱")
    val avatarEmoji: StateFlow<String> = _avatarEmoji.asStateFlow()

    private val _myName = MutableStateFlow("")
    val myName: StateFlow<String> = _myName.asStateFlow()

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)

    init {
        _avatarEmoji.value = prefs.getString("avatar_emoji", "🐱") ?: "🐱"
        _myName.value = prefs.getString("self_name", "") ?: ""
        checkExistingPair()
        _nicknameInput.value = prefs.getString("partner_nickname", "") ?: ""
        updatePartnerDisplayName()
        loadThemes()
    }

    fun selectAvatar(emoji: String) {
        _avatarEmoji.value = emoji
        prefs.edit().putString("avatar_emoji", emoji).apply()
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
            _loading.value = false
            return
        }
        viewModelScope.launch {
            try {
                val uid = supabaseService.getCachedDeviceId()
                val pair = pairingRepository.findPairByUserId(uid)
                if (pair != null) {
                    _isPaired.value = true
                    cacheAll(pair, uid)
                }
            } catch (_: Exception) { }
            _loading.value = false
        }
    }

    private fun cacheAll(pair: PairingInfo, myUid: String) {
        val partnerId = when {
            pair.user1Id == myUid -> pair.user2Id
            pair.user2Id == myUid -> pair.user1Id
            else -> ""
        }
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
            _status.value = "生成配对码中…"
            try {
                val uid = supabaseService.getCachedDeviceId()
                val myName = _nameInput.value.trim().ifEmpty { "我" }
                val code = pairingRepository.createPairingCode(uid, myName)
                _pairingCode.value = code
                _status.value = "配对码: $code，等待对方连接…"
                prefs.edit().putString("self_name", myName).apply()
                _myName.value = myName
                pollPairingComplete(code)
            } catch (e: Exception) {
                _status.value = "❌ 生成失败：${e.localizedMessage}"
            }
        }
    }

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
            } catch (_: Exception) { }
        }
    }

    fun updateJoinCode(code: String) { _joinCodeInput.value = code }

    fun joinPair() {
        viewModelScope.launch {
            _status.value = "正在连接…"
            try {
                val uid = supabaseService.getCachedDeviceId()
                val myName = _nameInput.value.trim().ifEmpty { "我" }
                val pairId = _joinCodeInput.value
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
                        putString("pair_id", pairInfo.pairId)
                        apply()
                    }
                    _myName.value = myName
                    updatePartnerDisplayName(partnerName = partnerName)
                } else {
                    _status.value = "❌ 配对失败，请检查配对码"
                }
            } catch (e: Exception) {
                _status.value = "❌ 连接失败：${e.localizedMessage}"
            }
        }
    }

    fun unpair() {
        viewModelScope.launch {
            try {
                val uid = supabaseService.getCachedDeviceId()
                pairingRepository.unpair(uid)
            } catch (_: Exception) { }
            prefs.edit().clear().apply()
            _avatarEmoji.value = "🐱"
            _isPaired.value = false
            _justPaired.value = false
            _pairingCode.value = ""
            _joinCodeInput.value = ""
            _status.value = "已解除配对"
            _partnerDisplayName.value = "对方"
        }
    }

    fun selectTheme(themeId: String) {
        EmojiThemeManager.setCurrentTheme(context, themeId)
        _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
    }

    fun dismissPairCelebration() {
        _justPaired.value = false
    }

    fun updateNameInput(name: String) { _nameInput.value = name }

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
}
