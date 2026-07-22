package com.standbyus.app.ui.checkin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.CheckinRepository
import com.standbyus.app.ui.settings.SettingsDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CheckinCalendarOwner {
    ME,
    PARTNER
}

data class CheckinCalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val recordsByDate: Map<LocalDate, List<CheckinData>> = emptyMap(),
    val recordOwner: CheckinCalendarOwner = CheckinCalendarOwner.ME,
    val partnerDisplayName: String = "对方",
    val isPaired: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedDateRecords: List<CheckinData>
        get() = recordsByDate[selectedDate].orEmpty()

    val selectedOwnerRecordsTitle: String
        get() = when (recordOwner) {
            CheckinCalendarOwner.ME -> "我的记录"
            CheckinCalendarOwner.PARTNER -> "${partnerDisplayName}的记录"
        }

    val selectedOwnerCheckinLabel: String
        get() = when (recordOwner) {
            CheckinCalendarOwner.ME -> "我的打卡"
            CheckinCalendarOwner.PARTNER -> "${partnerDisplayName}的打卡"
        }
}

@HiltViewModel
class CheckinCalendarViewModel @Inject constructor(
    private val checkinRepository: CheckinRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var partnerUserId = prefs.getString(KEY_PARTNER_ID, "").orEmpty()

    private val _uiState = MutableStateFlow(
        CheckinCalendarUiState(
            partnerDisplayName = resolvePartnerDisplayName(),
            isPaired = partnerUserId.isNotEmpty()
        )
    )
    val uiState: StateFlow<CheckinCalendarUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadMonth(YearMonth.now(), LocalDate.now())
    }

    fun showPreviousMonth() {
        val month = _uiState.value.visibleMonth.minusMonths(1)
        loadMonth(month, month.atDay(1))
    }

    fun showNextMonth() {
        val month = _uiState.value.visibleMonth.plusMonths(1)
        loadMonth(month, month.atDay(1))
    }

    fun returnToToday() {
        val today = LocalDate.now()
        loadMonth(YearMonth.from(today), today)
    }

    fun selectDate(date: LocalDate) {
        if (YearMonth.from(date) != _uiState.value.visibleMonth) return
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun selectRecordOwner(owner: CheckinCalendarOwner) {
        val state = _uiState.value
        if (owner == state.recordOwner) return
        if (owner == CheckinCalendarOwner.PARTNER && partnerUserId.isEmpty()) return

        _uiState.update { it.copy(recordOwner = owner) }
        loadMonth(
            month = state.visibleMonth,
            selectedDate = state.selectedDate,
            owner = owner
        )
    }

    fun refreshPairingState() {
        val previousPartnerId = partnerUserId
        partnerUserId = prefs.getString(KEY_PARTNER_ID, "").orEmpty()
        val isPaired = partnerUserId.isNotEmpty()
        val partnerDisplayName = resolvePartnerDisplayName()
        val current = _uiState.value

        if (!isPaired && current.recordOwner == CheckinCalendarOwner.PARTNER) {
            _uiState.update {
                it.copy(
                    recordOwner = CheckinCalendarOwner.ME,
                    partnerDisplayName = partnerDisplayName,
                    isPaired = false
                )
            }
            loadMonth(current.visibleMonth, current.selectedDate, CheckinCalendarOwner.ME)
            return
        }

        _uiState.update {
            it.copy(
                partnerDisplayName = partnerDisplayName,
                isPaired = isPaired
            )
        }

        if (
            current.recordOwner == CheckinCalendarOwner.PARTNER &&
            previousPartnerId != partnerUserId
        ) {
            loadMonth(current.visibleMonth, current.selectedDate, CheckinCalendarOwner.PARTNER)
        }
    }

    fun retry() {
        val state = _uiState.value
        loadMonth(state.visibleMonth, state.selectedDate, state.recordOwner)
    }

    private fun loadMonth(
        month: YearMonth,
        selectedDate: LocalDate,
        owner: CheckinCalendarOwner = _uiState.value.recordOwner
    ) {
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                visibleMonth = month,
                selectedDate = selectedDate,
                recordOwner = owner,
                recordsByDate = emptyMap(),
                isLoading = true,
                errorMessage = null
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val userId = when (owner) {
                    CheckinCalendarOwner.ME -> supabaseService.getCachedDeviceId()
                    CheckinCalendarOwner.PARTNER -> partnerUserId
                }
                if (userId.isEmpty()) {
                    _uiState.update { current ->
                        if (current.visibleMonth != month || current.recordOwner != owner) {
                            current
                        } else {
                            current.copy(
                                isLoading = false,
                                errorMessage = if (owner == CheckinCalendarOwner.PARTNER) {
                                    "绑定对方后才能查看记录"
                                } else {
                                    "设备信息尚未准备好，请稍后重试"
                                }
                            )
                        }
                    }
                    return@launch
                }
                val range = checkinMonthRange(month)
                val records = checkinRepository.getCheckInsInRange(
                    userId = userId,
                    startInclusive = range.startInclusive,
                    endExclusive = range.endExclusive
                )
                _uiState.update { current ->
                    if (current.visibleMonth != month || current.recordOwner != owner) current else current.copy(
                        recordsByDate = groupCheckInsByLocalDate(records),
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { current ->
                    if (current.visibleMonth != month || current.recordOwner != owner) current else current.copy(
                        isLoading = false,
                        errorMessage = "记录加载失败，请稍后重试"
                    )
                }
            }
        }
    }

    private fun resolvePartnerDisplayName(): String {
        return SettingsDisplayName.resolvePartnerDisplayName(
            nickname = prefs.getString(KEY_PARTNER_NICKNAME, ""),
            partnerName = prefs.getString(KEY_PARTNER_NAME, "")
        )
    }

    companion object {
        private const val PREFS_NAME = "pairing"
        private const val KEY_PARTNER_ID = "partner_id"
        private const val KEY_PARTNER_NAME = "partner_name"
        private const val KEY_PARTNER_NICKNAME = "partner_nickname"
    }
}
