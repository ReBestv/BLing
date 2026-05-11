package com.standbyus.app.ui.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val supabaseService: SupabaseService,
    private val pairingRepository: PairingRepository
) : ViewModel() {

    private val _statusHistory = MutableStateFlow<List<UserStatus>>(emptyList())
    val statusHistory: StateFlow<List<UserStatus>> = _statusHistory.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _partnerName = MutableStateFlow("对方")
    val partnerName: StateFlow<String> = _partnerName.asStateFlow()

    init {
        loadHistory()
    }

    fun refresh() { loadHistory() }

    private fun loadHistory() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val myId = supabaseService.getCachedDeviceId()
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

                // 获取对方的状态
                val pair = pairingRepository.findPairByUserId(myId)
                val partnerHistory = if (pair != null) {
                    val partnerId = when {
                        pair.user1Id == myId && pair.user2Id.isNotEmpty() -> pair.user2Id
                        pair.user2Id == myId && pair.user1Id.isNotEmpty() -> pair.user1Id
                        else -> null
                    }
                    if (partnerId != null) {
                        val pResults = supabaseService.query(
                            "statuses",
                            "userId=eq.$partnerId&order=updatedAt.desc&limit=30"
                        )
                        pResults.mapNotNull { raw ->
                            try { UserStatus.fromMap(raw) } catch (_: Exception) { null }
                        }
                    } else emptyList()
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

    companion object {
        private const val TAG = "StandByHistoryVM"
    }
}
