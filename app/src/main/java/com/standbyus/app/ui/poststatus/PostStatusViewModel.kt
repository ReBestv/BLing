package com.standbyus.app.ui.poststatus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.Doing
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostStatusViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val supabaseService: SupabaseService
) : ViewModel() {

    var selectedFeeling by mutableStateOf(Feeling.HAPPY)
        private set
    var selectedDoing by mutableStateOf("搬砖")
        private set
    var customDoing by mutableStateOf("")
        private set
    var note by mutableStateOf("")
        private set
    var isPublishing by mutableStateOf(false)
        private set
    var published by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set

    fun selectFeeling(feeling: Feeling) { selectedFeeling = feeling }
    fun selectDoing(doing: String) { selectedDoing = doing }
    fun updateCustomDoing(value: String) { customDoing = value }
    fun updateNote(value: String) { note = value }

    fun publish() {
        viewModelScope.launch {
            isPublishing = true
            error = ""

            val userId = supabaseService.getCachedDeviceId()

            val status = UserStatus(
                userId = userId,
                doing = selectedDoing,
                customDoing = customDoing,
                feeling = selectedFeeling.displayName,
                feelingColor = selectedFeeling.color.toString(),
                feelingEmoji = selectedFeeling.emoji,
                note = note
            )
            try {
                statusRepository.updateStatus(status)
                isPublishing = false
                published = true
            } catch (e: Exception) {
                error = "发布失败：${e.localizedMessage}"
                isPublishing = false
            }
        }
    }
}
