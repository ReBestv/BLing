package com.standbyus.app.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.Interaction
import com.standbyus.app.data.model.InteractionType
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.InteractionRepository
import com.standbyus.app.data.repository.PairingRepository
import com.standbyus.app.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val pairingRepository: PairingRepository,
    private val interactionRepository: InteractionRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)

    private val _myUserId = MutableStateFlow("")
    private val _partnerUserId = MutableStateFlow("")

    val myStatus: StateFlow<UserStatus?> = _myUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else statusRepository.observeStatus(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val partnerStatus: StateFlow<UserStatus?> = _partnerUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else statusRepository.observeStatus(id)
    }.onEach { status ->
        if (status != null) {
            statusRepository.updateWidgetCache(status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _partnerDisplayName = MutableStateFlow("对方")
    val partnerDisplayName: StateFlow<String> = _partnerDisplayName.asStateFlow()

    val latestInteraction: StateFlow<Interaction?> = _myUserId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else interactionRepository.observeLatestReceivedInteraction(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _sendingInteractionType = MutableStateFlow<String?>(null)
    val sendingInteractionType: StateFlow<String?> = _sendingInteractionType.asStateFlow()

    private val _interactionError = MutableStateFlow("")
    val interactionError: StateFlow<String> = _interactionError.asStateFlow()

    init {
        try {
            val uid = supabaseService.getCachedDeviceId()
            Log.d(TAG, "deviceId=$uid")
            _myUserId.value = uid

            // 优先从缓存读取 partner ID
            val cachedPartnerId = prefs.getString("partner_id", null)
            if (!cachedPartnerId.isNullOrEmpty()) {
                Log.d(TAG, "partner from cache: $cachedPartnerId")
                _partnerUserId.value = cachedPartnerId
                val cachedNickname = prefs.getString("partner_nickname", null)
                val cachedPartnerName = prefs.getString("partner_name", null)
                _partnerDisplayName.value = when {
                    !cachedNickname.isNullOrEmpty() -> cachedNickname
                    !cachedPartnerName.isNullOrEmpty() -> cachedPartnerName
                    else -> "对方"
                }
            } else {
                // 无缓存时联网查询
                viewModelScope.launch {
                    try {
                        Log.d(TAG, "finding pair for $uid")
                        val pair = pairingRepository.findPairByUserId(uid)
                        Log.d(TAG, "pair=$pair")
                        if (pair != null) {
                            val partnerId = when {
                                pair.user1Id == uid && pair.user2Id.isNotEmpty() -> pair.user2Id
                                pair.user2Id == uid && pair.user1Id.isNotEmpty() -> pair.user1Id
                                else -> ""
                            }
                            if (partnerId.isNotEmpty()) {
                                prefs.edit().putString("partner_id", partnerId).apply()
                                _partnerUserId.value = partnerId
                            }
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
                    } catch (e: Exception) {
                        Log.e(TAG, "pair lookup failed", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "init failed", e)
        }
        Log.d(TAG, "init end")
    }

    fun setPartnerId(partnerId: String) {
        _partnerUserId.value = partnerId
    }

    fun sendInteraction(type: InteractionType) {
        val fromUserId = _myUserId.value
        val toUserId = _partnerUserId.value
        if (fromUserId.isEmpty() || toUserId.isEmpty()) return

        viewModelScope.launch {
            _sendingInteractionType.value = type.key
            _interactionError.value = ""
            try {
                interactionRepository.sendInteraction(
                    fromUserId = fromUserId,
                    toUserId = toUserId,
                    type = type,
                    targetStatusTime = partnerStatus.value?.updatedAt ?: 0L
                )
            } catch (e: Exception) {
                Log.e(TAG, "send interaction failed", e)
                _interactionError.value = "刚刚没送达，等下再试试"
            } finally {
                _sendingInteractionType.value = null
            }
        }
    }

    fun markLatestInteractionRead() {
        val interaction = latestInteraction.value ?: return
        if (interaction.readAt != null) return
        viewModelScope.launch {
            try {
                interactionRepository.markRead(interaction.id)
            } catch (e: Exception) {
                Log.e(TAG, "mark interaction read failed", e)
            }
        }
    }

    companion object {
        private const val TAG = "StandByHomeVM"
    }
}
