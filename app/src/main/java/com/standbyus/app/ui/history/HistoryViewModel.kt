package com.standbyus.app.ui.history

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.PairingRepository
import com.standbyus.app.ui.settings.SettingsDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val supabaseService: SupabaseService,
    private val pairingRepository: PairingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)
    private val avatarPreferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_AVATAR_EMOJI || key == KEY_AVATAR_URL) {
                refreshMyAvatarSnapshot()
            }
        }

    private val _statusHistory = MutableStateFlow<List<UserStatus>>(emptyList())
    val statusHistory: StateFlow<List<UserStatus>> = _statusHistory.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _partnerName = MutableStateFlow(resolvePartnerName())
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    private val _myId = MutableStateFlow("")
    val myId: StateFlow<String> = _myId.asStateFlow()

    private val _myAvatar = MutableStateFlow(
        readMyAvatar()
    )
    val myAvatar: StateFlow<String> = _myAvatar.asStateFlow()

    private val _myAvatarUrl = MutableStateFlow(
        readMyAvatarUrl()
    )
    val myAvatarUrl: StateFlow<String> = _myAvatarUrl.asStateFlow()

    init {
        prefs.registerOnSharedPreferenceChangeListener(avatarPreferenceListener)
        loadHistory()
    }

    fun refresh() {
        refreshMyAvatarSnapshot()
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _loading.value = true
            try {
                refreshMyAvatarSnapshot()
                val myId = supabaseService.getCachedDeviceId()
                _myId.value = myId
                _partnerName.value = resolvePartnerName()
                if (myId.isEmpty()) {
                    _statusHistory.value = emptyList()
                    _loading.value = false
                    return@launch
                }

                val myResults = supabaseService.query(
                    "statuses",
                    "userId=eq.$myId&order=updatedAt.desc&limit=20"
                )
                val myHistory = myResults.mapNotNull { raw ->
                    try {
                        UserStatus.fromMap(raw)
                    } catch (_: Exception) {
                        null
                    }
                }

                val pair = runCatching { pairingRepository.findPairByUserId(myId) }
                    .onFailure { Log.e(TAG, "pair lookup failed", it) }
                    .getOrNull()
                val partnerId = when {
                    pair?.user1Id == myId && pair.user2Id.isNotEmpty() -> pair.user2Id
                    pair?.user2Id == myId && pair.user1Id.isNotEmpty() -> pair.user1Id
                    else -> ""
                }

                val partnerHistory = if (partnerId.isNotEmpty()) {
                    prefs.edit().putString(KEY_PARTNER_ID, partnerId).apply()
                    val partnerName = when {
                        pair?.user1Id == myId -> pair.user2Name
                        pair?.user2Id == myId -> pair.user1Name
                        else -> ""
                    }
                    if (partnerName.isNotEmpty()) {
                        prefs.edit().putString(KEY_PARTNER_NAME, partnerName).apply()
                    }
                    _partnerName.value = resolvePartnerName()

                    val results = supabaseService.query(
                        "statuses",
                        "userId=eq.$partnerId&order=updatedAt.desc&limit=30"
                    )
                    results.mapNotNull { raw ->
                        try {
                            UserStatus.fromMap(raw)
                        } catch (_: Exception) {
                            null
                        }
                    }
                } else {
                    clearPartnerCache()
                    _partnerName.value = resolvePartnerName()
                    emptyList()
                }

                _statusHistory.value = (myHistory + partnerHistory)
                    .sortedByDescending { it.updatedAt }
            } catch (e: Exception) {
                Log.e(TAG, "loadHistory failed", e)
            }
            _loading.value = false
        }
    }

    private fun readMyAvatar(): String {
        return prefs.getString(KEY_AVATAR_EMOJI, DEFAULT_AVATAR)
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_AVATAR
    }

    private fun readMyAvatarUrl(): String {
        return prefs.getString(KEY_AVATAR_URL, "") ?: ""
    }

    private fun refreshMyAvatarSnapshot() {
        _myAvatar.value = readMyAvatar()
        _myAvatarUrl.value = readMyAvatarUrl()
    }

    private fun resolvePartnerName(): String {
        return SettingsDisplayName.resolvePartnerDisplayName(
            nickname = prefs.getString("partner_nickname", ""),
            partnerName = prefs.getString("partner_name", "")
        )
    }

    private fun clearPartnerCache() {
        prefs.edit().apply {
            remove(KEY_PARTNER_ID)
            remove(KEY_PARTNER_NAME)
            remove("partner_nickname")
            remove("pair_id")
            remove("is_paired")
            apply()
        }
    }

    companion object {
        private const val TAG = "StandByHistoryVM"
        private const val KEY_PARTNER_ID = "partner_id"
        private const val KEY_PARTNER_NAME = "partner_name"
        private const val KEY_AVATAR_EMOJI = "avatar_emoji"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val DEFAULT_AVATAR = "\uD83D\uDE42"
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(avatarPreferenceListener)
        super.onCleared()
    }
}
