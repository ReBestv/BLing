package com.standbyus.app.ui.history

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

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val myId = supabaseService.getCachedDeviceId()
            val pair = pairingRepository.findPairByUserId(myId)
            val partnerId = when {
                pair?.user1Id == myId && pair.user2Id.isNotEmpty() -> pair.user2Id
                pair?.user2Id == myId && pair.user1Id.isNotEmpty() -> pair.user1Id
                else -> return@launch
            }

            val results = supabaseService.query(
                "statuses",
                "deviceId=eq.$partnerId&order=updatedAt.desc&limit=50"
            )
            _statusHistory.value = results.mapNotNull { raw ->
                try {
                    UserStatus.fromMap(raw)
                } catch (_: Exception) { null }
            }
        }
    }
}
