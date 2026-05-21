package com.standbyus.app.ui.poststatus

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.data.model.ThemeSticker
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.StatusRepository
import com.standbyus.app.ui.theme.EmojiThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostStatusViewModel @Inject constructor(
    private val statusRepository: StatusRepository,
    private val supabaseService: SupabaseService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var selectedFeeling by mutableStateOf(Feeling.HAPPY)
        private set
    var customDoing by mutableStateOf("")
        private set
    var selectedStickerId by mutableStateOf<String?>(null)
        private set
    var isPublishing by mutableStateOf(false)
        private set
    var published by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set

    fun selectFeeling(feeling: Feeling) { selectedFeeling = feeling }
    fun selectSticker(sticker: ThemeSticker) {
        selectedStickerId = if (selectedStickerId == sticker.id) null else sticker.id
    }
    fun updateCustomDoing(value: String) { customDoing = value }

    fun publish() {
        viewModelScope.launch {
            isPublishing = true
            error = ""

            val userId = supabaseService.getCachedDeviceId()
            val snapshot = EmojiThemeManager.createSnapshot(
                theme = EmojiThemeManager.getCurrentTheme(context),
                feelingKey = selectedFeeling.key,
                stickerId = selectedStickerId
            )

            val status = UserStatus(
                userId = userId,
                doing = customDoing.ifEmpty { snapshot.feelingLabel },
                customDoing = customDoing,
                themeId = snapshot.themeId,
                themeName = snapshot.themeName,
                feelingKey = snapshot.feelingKey,
                feelingLabel = snapshot.feelingLabel,
                feelingAsset = snapshot.feelingAsset,
                feelingFallbackEmoji = snapshot.feelingFallbackEmoji,
                feelingColor = snapshot.feelingColor,
                stickerId = snapshot.stickerId,
                stickerLabel = snapshot.stickerLabel,
                stickerAsset = snapshot.stickerAsset,
                note = ""
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
