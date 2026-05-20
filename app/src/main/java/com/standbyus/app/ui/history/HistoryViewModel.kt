package com.standbyus.app.ui.history

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.PairingRepository
import com.standbyus.app.ui.settings.SettingsDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val supabaseService: SupabaseService,
    private val pairingRepository: PairingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)

    private val _statusHistory = MutableStateFlow<List<UserStatus>>(emptyList())
    val statusHistory: StateFlow<List<UserStatus>> = _statusHistory.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _partnerName = MutableStateFlow(resolvePartnerName())
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    private val _myId = MutableStateFlow("")
    val myId: StateFlow<String> = _myId.asStateFlow()

    private val _myAvatar = MutableStateFlow(
        prefs.getString("avatar_emoji", "🐱") ?: "🐱"
    )
    val myAvatar: StateFlow<String> = _myAvatar.asStateFlow()

    init {
        loadHistory()
    }

    fun refresh() { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val myId = supabaseService.getCachedDeviceId()
                _myId.value = myId
                _partnerName.value = resolvePartnerName()
                if (myId.isEmpty()) {
                    _loading.value = false
                    return@launch
                }

                // 获取自己的状态
                val myResults = supabaseService.query(
                    "statuses",
                    "userId=eq.$myId&order=updatedAt.desc&limit=20"
                )
                val myHistory = myResults.mapNotNull { raw ->
                    try { UserStatus.fromMap(raw) } catch (_: Exception) { null }
                }

                // 获取对方的状态 — 优先使用缓存的 partner_id
                val cachedPartnerId = prefs.getString("partner_id", null)
                val partnerId = if (!cachedPartnerId.isNullOrEmpty()) {
                    cachedPartnerId
                } else {
                    val pair = pairingRepository.findPairByUserId(myId)
                    when {
                        pair?.user1Id == myId && pair.user2Id.isNotEmpty() -> pair.user2Id
                        pair?.user2Id == myId && pair.user1Id.isNotEmpty() -> pair.user1Id
                        else -> null
                    }
                }

                val partnerHistory = if (partnerId != null) {
                    val pResults = supabaseService.query(
                        "statuses",
                        "userId=eq.$partnerId&order=updatedAt.desc&limit=30" // 扩大显示
                    )
                    pResults.mapNotNull { raw ->
                        try { UserStatus.fromMap(raw) } catch (_: Exception) { null }
                    }
                } else emptyList()

                // 合并并按时间降序排序
                _statusHistory.value = (myHistory + partnerHistory)
                    .sortedByDescending { it.updatedAt }
            } catch (e: Exception) {
                Log.e(TAG, "loadHistory failed", e)
            }
            _loading.value = false
        }
    }

    private fun resolvePartnerName(): String {
        return SettingsDisplayName.resolvePartnerDisplayName(
            nickname = prefs.getString("partner_nickname", ""),
            partnerName = prefs.getString("partner_name", "")
        )
    }

    companion object {
        private const val TAG = "StandByHistoryVM"
    }
}
