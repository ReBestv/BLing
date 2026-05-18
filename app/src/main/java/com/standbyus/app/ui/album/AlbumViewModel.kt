package com.standbyus.app.ui.album

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.AlbumPhoto
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.AlbumRepository
import com.standbyus.app.data.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val supabaseService: SupabaseService,
    private val pairingRepository: PairingRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("pairing", Context.MODE_PRIVATE)

    private val _photos = MutableStateFlow<List<AlbumPhoto>>(emptyList())
    val photos: StateFlow<List<AlbumPhoto>> = _photos.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    private val _showUploadSheet = MutableStateFlow(false)
    val showUploadSheet: StateFlow<Boolean> = _showUploadSheet.asStateFlow()

    private val _selectedUri = MutableStateFlow<Uri?>(null)
    val selectedUri: StateFlow<Uri?> = _selectedUri.asStateFlow()

    private val _caption = MutableStateFlow("")
    val caption: StateFlow<String> = _caption.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _filterDays = MutableStateFlow(0) // 0=全部, 7=最近7天, 30=最近30天
    val filterDays: StateFlow<Int> = _filterDays.asStateFlow()

    init { loadPhotos() }

    fun setFilter(days: Int) {
        _filterDays.value = days
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val myId = supabaseService.getCachedDeviceId()
                Log.d(TAG, "loadPhotos myId=$myId")
                if (myId.isEmpty()) { Log.w(TAG, "myId empty"); _loading.value = false; return@launch }

                // 优先使用缓存的 partner_id
                val cachedPartnerId = prefs.getString("partner_id", null)
                val partnerId = if (!cachedPartnerId.isNullOrEmpty()) {
                    cachedPartnerId
                } else {
                    val pair = pairingRepository.findPairByUserId(myId)
                    Log.d(TAG, "pair=$pair")
                    when {
                        pair?.user1Id == myId && pair.user2Id.isNotEmpty() -> pair.user2Id
                        pair?.user2Id == myId && pair.user1Id.isNotEmpty() -> pair.user1Id
                        else -> ""
                    }
                }
                Log.d(TAG, "partnerId=$partnerId")

                val since = if (_filterDays.value > 0) System.currentTimeMillis() - _filterDays.value * 86400000L else 0L
                _photos.value = albumRepository.getPhotos(myId, partnerId, sinceTimestamp = since)
                Log.d(TAG, "photos count=${_photos.value.size}")
            } catch (e: Exception) {
                Log.e(TAG, "loadPhotos failed", e)
            }
            _loading.value = false
        }
    }

    fun selectPhoto(uri: Uri) {
        _selectedUri.value = uri
        _showUploadSheet.value = true
    }

    fun updateCaption(text: String) {
        if (text.length <= 50) _caption.value = text
    }

    fun dismissSheet() {
        _showUploadSheet.value = false
        _selectedUri.value = null
        _caption.value = ""
    }

    fun upload() {
        val uri = _selectedUri.value ?: return
        val caption = _caption.value
        viewModelScope.launch {
            _uploading.value = true
            _error.value = ""
            try {
                val myId = supabaseService.getCachedDeviceId()
                albumRepository.uploadPhoto(uri, caption, myId)
                dismissSheet()
                loadPhotos()
            } catch (e: Exception) {
                _error.value = "上传失败：${e.localizedMessage}"
                Log.e(TAG, "upload failed", e)
            }
            _uploading.value = false
        }
    }

    fun deletePhoto(photo: AlbumPhoto) {
        viewModelScope.launch {
            try {
                albumRepository.deletePhoto(photo)
                _photos.value = _photos.value.filter { it.id != photo.id }
            } catch (e: Exception) {
                val msg = e.message ?: e.javaClass.simpleName
                Log.e(TAG, "deletePhoto failed", e)
                _error.value = "删除失败：$msg"
            }
        }
    }

    companion object {
        private const val TAG = "StandByAlbumVM"
    }
}
