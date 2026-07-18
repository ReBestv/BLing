package com.standbyus.app.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.CheckinData
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.CheckinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckinCalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val recordsByDate: Map<LocalDate, List<CheckinData>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedDateRecords: List<CheckinData>
        get() = recordsByDate[selectedDate].orEmpty()
}

@HiltViewModel
class CheckinCalendarViewModel @Inject constructor(
    private val checkinRepository: CheckinRepository,
    private val supabaseService: SupabaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckinCalendarUiState())
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

    fun retry() {
        val state = _uiState.value
        loadMonth(state.visibleMonth, state.selectedDate)
    }

    private fun loadMonth(month: YearMonth, selectedDate: LocalDate) {
        loadJob?.cancel()
        _uiState.update {
            it.copy(
                visibleMonth = month,
                selectedDate = selectedDate,
                recordsByDate = emptyMap(),
                isLoading = true,
                errorMessage = null
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val userId = supabaseService.getCachedDeviceId()
                val range = checkinMonthRange(month)
                val records = checkinRepository.getCheckInsInRange(
                    userId = userId,
                    startInclusive = range.startInclusive,
                    endExclusive = range.endExclusive
                )
                _uiState.update { current ->
                    if (current.visibleMonth != month) current else current.copy(
                        recordsByDate = groupCheckInsByLocalDate(records),
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { current ->
                    if (current.visibleMonth != month) current else current.copy(
                        isLoading = false,
                        errorMessage = "记录加载失败，请稍后重试"
                    )
                }
            }
        }
    }
}
