package com.standbyus.app.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.PairingInfo
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.standbyus.app.ui.theme.EmojiTheme
import com.standbyus.app.ui.theme.EmojiThemeManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val supabaseService: SupabaseService,
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
    val currentTheme: StateFlow<EmojiTheme> = _currentTheme.asStateFlow()

    val availableThemes = EmojiThemeManager.themes

    private val _justPaired = MutableStateFlow(false)
    val justPaired: StateFlow<Boolean> = _justPaired.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)

    init {
        checkExistingPair()
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
        prefs.edit().apply {
            putBoolean("is_paired", true)
            putString("partner_id", partnerId)
            apply()
        }
    }

    fun createCode() {
        viewModelScope.launch {
            _status.value = "生成配对码中…"
            try {
                val uid = supabaseService.getCachedDeviceId()
                val code = pairingRepository.createPairingCode(uid)
                _pairingCode.value = code
                _status.value = "配对码: $code，等待对方连接…"

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
                val result = pairingRepository.joinPair(_joinCodeInput.value, uid)
                if (result.success) {
                    _isPaired.value = true
                    _justPaired.value = true
                    _status.value = "✅ 配对成功！"
                    prefs.edit().apply {
                        putBoolean("is_paired", true)
                        putString("partner_id", result.partnerId)
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

    fun unpair() {
        viewModelScope.launch {
            try {
                val uid = supabaseService.getCachedDeviceId()
                pairingRepository.unpair(uid)
            } catch (_: Exception) { }
            prefs.edit().clear().apply()
            _isPaired.value = false
            _justPaired.value = false
            _pairingCode.value = ""
            _joinCodeInput.value = ""
            _status.value = "已解除配对"
        }
    }

    fun selectTheme(themeId: String) {
        EmojiThemeManager.setCurrentTheme(context, themeId)
        _currentTheme.value = EmojiThemeManager.getCurrentTheme(context)
    }

    fun dismissPairCelebration() {
        _justPaired.value = false
    }
}
