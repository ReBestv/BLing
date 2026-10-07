package com.standbyus.app.ui.checkin

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.InteractionType
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.CheckinRepository
import com.standbyus.app.data.repository.CheckinFailure
import com.standbyus.app.data.repository.CheckinSubmitResult
import com.standbyus.app.data.repository.InteractionRepository
import com.standbyus.app.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class CheckinUiState(
    val todayCount: Int = 0,
    val lastInterval: String = "-",
    val riskLevel: RiskLevel = RiskLevel.NORMAL,
    val weeklyTotal: Int = 0,
    val weeklyAverage: Float = 0f,
    val streak: Int = 0,
    val isCheckingIn: Boolean = false,
    val isPaired: Boolean = false,
    val pkStats: PKStats? = null,
    val feedback: String = "",
    val feedbackIsError: Boolean = false,
    val syncMessage: String = ""
)

enum class RiskLevel(val label: String, val emoji: String) {
    NORMAL("正常", "🙂"),
    MILD("轻度", "😵"),
    ATTENTION("注意", "😳")
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
    private val interactionRepository: InteractionRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _myUserId = MutableStateFlow("")
    private val _partnerUserId = MutableStateFlow("")
    private val _isCheckingIn = MutableStateFlow(false)
    private val _submitResult = MutableStateFlow<CheckinSubmitResult?>(null)
    val uiState: StateFlow<CheckinUiState> = combine(_myUserId, _partnerUserId) { myId, partnerId ->
        myId to partnerId
    }.flatMapLatest { (myId, partnerId) ->
        if (myId.isEmpty()) return@flatMapLatest flowOf(CheckinUiState())
        combine(
            checkinRepository.observeCheckIns(myId),
            if (partnerId.isEmpty()) flowOf(emptyList()) else checkinRepository.observeCheckIns(partnerId),
            _isCheckingIn,
            _submitResult,
            checkinRepository.syncErrors
        ) { _, _, checkingIn, result, syncErrors ->
            val todayCount = checkinRepository.getTodayCount(myId)
            val lastCheckinTime = checkinRepository.getLastCheckinTime(myId)
            val weekTotal = checkinRepository.getWeekCount(myId)
            val streak = checkinRepository.getStreakDays(myId)
            val weekAverage = calculateWeekAverage(weekTotal)
            val interval = CheckinIntervalFormatter.sinceLastCheckin(
                lastCheckinTime = lastCheckinTime,
                now = System.currentTimeMillis()
            )
            val riskLevel = calculateRiskLevel(lastCheckinTime)
            val pkStats = if (partnerId.isNotEmpty()) {
                val myMonthCount = checkinRepository.getMonthCount(myId)
                val partnerMonthCount = checkinRepository.getMonthCount(partnerId)
                PKStats(myMonthCount, partnerMonthCount)
            } else {
                null
            }

            CheckinUiState(
                todayCount = todayCount,
                lastInterval = interval,
                riskLevel = riskLevel,
                weeklyTotal = weekTotal,
                weeklyAverage = weekAverage,
                streak = streak,
                isCheckingIn = checkingIn,
                isPaired = partnerId.isNotEmpty(),
                pkStats = pkStats,
                feedback = when (result) {
                    is CheckinSubmitResult.Saved -> if (result.cached) "打卡已记录" else "打卡已保存，正在重新加载记录"
                    is CheckinSubmitResult.Failed -> result.reason.submitMessage
                    null -> ""
                },
                feedbackIsError = result is CheckinSubmitResult.Failed,
                syncMessage = when {
                    syncErrors[myId] == CheckinFailure.CONFIGURATION || syncErrors[partnerId] == CheckinFailure.CONFIGURATION ->
                        "当前版本无法连接同步服务，原配对和已有记录仍保留"
                    syncErrors.containsKey(myId) -> "打卡记录同步暂时失败，保留已有记录并自动重试"
                    syncErrors.containsKey(partnerId) -> "对方打卡同步暂时失败，保留已有记录并自动重试"
                    else -> ""
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CheckinUiState())

    init {
        val uid = supabaseService.getDeviceId(context)
        Log.d(TAG, "deviceId=$uid")
        _myUserId.value = uid
        refreshPairingState()
    }

    fun refreshPairingState() {
        val uid = _myUserId.value.ifEmpty { supabaseService.getCachedDeviceId() }
        if (uid.isEmpty()) {
            clearPartnerState()
            return
        }

        val cachedPartnerId = prefs.getString(KEY_PARTNER_ID, null)
        if (!cachedPartnerId.isNullOrEmpty()) {
            _partnerUserId.value = cachedPartnerId
        }

        viewModelScope.launch {
            try {
                val pair = pairingRepository.findPairByUserId(uid)
                val partnerId = when {
                    pair?.user1Id == uid && pair.user2Id.isNotEmpty() -> pair.user2Id
                    pair?.user2Id == uid && pair.user1Id.isNotEmpty() -> pair.user1Id
                    else -> ""
                }
                if (partnerId.isEmpty()) {
                    clearPartnerState()
                    return@launch
                }
                prefs.edit().apply {
                    putBoolean(KEY_IS_PAIRED, true)
                    putString(KEY_PARTNER_ID, partnerId)
                    putString(KEY_PAIR_ID, pair?.pairId)
                    apply()
                }
                _partnerUserId.value = partnerId
            } catch (e: Exception) {
                Log.e(TAG, "refreshPairingState failed", e)
                if (cachedPartnerId.isNullOrEmpty()) {
                    clearPartnerState()
                }
            }
        }
    }

    fun checkIn() {
        if (_isCheckingIn.value) return
        _submitResult.value = null
        _isCheckingIn.value = true
        viewModelScope.launch {
            try {
                val result = checkinRepository.submitCheckIn()
                _submitResult.value = result
                if (result is CheckinSubmitResult.Saved) {
                    viewModelScope.launch { sendPoopCheckinInteraction() }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "checkIn failed", e)
                _submitResult.value = CheckinSubmitResult.Failed(CheckinFailure.from(e))
            } finally {
                _isCheckingIn.value = false
            }
        }
    }

    private suspend fun sendPoopCheckinInteraction() {
        val fromUserId = _myUserId.value
        val toUserId = _partnerUserId.value
        if (fromUserId.isEmpty() || toUserId.isEmpty()) return

        try {
            interactionRepository.sendInteraction(
                fromUserId = fromUserId,
                toUserId = toUserId,
                type = InteractionType.POOP_CHECKIN,
                targetStatusTime = System.currentTimeMillis()
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Log.e(TAG, "poop checkin interaction failed", e)
        }
    }

    private fun clearPartnerState() {
        prefs.edit().apply {
            remove(KEY_IS_PAIRED)
            remove(KEY_PARTNER_ID)
            remove(KEY_PAIR_ID)
            apply()
        }
        _partnerUserId.value = ""
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
        val daysPassed = LocalDate.now().dayOfWeek.value
        if (daysPassed == 0) return 0f
        return kotlin.math.round(weekTotal.toFloat() / daysPassed * 10) / 10f
    }

    companion object {
        private const val TAG = "CheckinVM"
        private const val PREFS_NAME = "pairing"
        private const val KEY_IS_PAIRED = "is_paired"
        private const val KEY_PARTNER_ID = "partner_id"
        private const val KEY_PAIR_ID = "pair_id"
    }
}
