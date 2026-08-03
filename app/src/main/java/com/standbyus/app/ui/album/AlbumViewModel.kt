package com.standbyus.app.ui.album

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.standbyus.app.data.model.AlbumComment
import com.standbyus.app.data.model.AlbumPhoto
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.data.repository.AlbumCommentRepository
import com.standbyus.app.data.repository.AlbumRepository
import com.standbyus.app.data.repository.PairingRepository
import com.standbyus.app.data.repository.StatusRepository
import com.standbyus.app.ui.settings.SettingsDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val albumCommentRepository: AlbumCommentRepository,
    private val supabaseService: SupabaseService,
    private val pairingRepository: PairingRepository,
    private val statusRepository: StatusRepository,
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

    private val _filterDays = MutableStateFlow(0)
    val filterDays: StateFlow<Int> = _filterDays.asStateFlow()

    private val _comments = MutableStateFlow<List<AlbumComment>>(emptyList())
    val comments: StateFlow<List<AlbumComment>> = _comments.asStateFlow()

    private val _activeCommentPhotoId = MutableStateFlow<Long?>(null)
    val activeCommentPhotoId: StateFlow<Long?> = _activeCommentPhotoId.asStateFlow()

    private val _commentCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<Long, Int>> = _commentCounts.asStateFlow()

    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading.asStateFlow()

    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _sendingComment = MutableStateFlow(false)
    val sendingComment: StateFlow<Boolean> = _sendingComment.asStateFlow()

    private val _deletingCommentId = MutableStateFlow<Long?>(null)
    val deletingCommentId: StateFlow<Long?> = _deletingCommentId.asStateFlow()

    private val _commentError = MutableStateFlow("")
    val commentError: StateFlow<String> = _commentError.asStateFlow()

    private val _myDeviceId = MutableStateFlow("")
    val myDeviceId: StateFlow<String> = _myDeviceId.asStateFlow()

    private val _myCommentProfile = MutableStateFlow(AlbumCommentProfile())
    val myCommentProfile: StateFlow<AlbumCommentProfile> = _myCommentProfile.asStateFlow()

    private val _partnerCommentProfile = MutableStateFlow(
        AlbumCommentProfile(displayName = DEFAULT_PARTNER_NAME)
    )
    val partnerCommentProfile: StateFlow<AlbumCommentProfile> = _partnerCommentProfile.asStateFlow()

    private var commentsPollingJob: Job? = null
    private var partnerProfileJob: Job? = null
    private var observedPartnerId: String = ""

    init {
        loadPhotos()
    }

    fun refresh() {
        loadPhotos()
    }

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
                if (myId.isEmpty()) {
                    _photos.value = emptyList()
                    clearCommentIdentity()
                    _loading.value = false
                    return@launch
                }

                val cachedPartnerId = prefs.getString(KEY_PARTNER_ID, null)
                val pair = runCatching { pairingRepository.findPairByUserId(myId) }
                    .onFailure { Log.e(TAG, "pair lookup failed", it) }
                    .getOrNull()
                val partnerId = when {
                    !cachedPartnerId.isNullOrEmpty() -> cachedPartnerId
                    pair?.user1Id == myId && pair.user2Id.isNotEmpty() -> pair.user2Id
                    pair?.user2Id == myId && pair.user1Id.isNotEmpty() -> pair.user1Id
                    else -> ""
                }

                if (partnerId.isEmpty()) {
                    prefs.edit().apply {
                        remove(KEY_PARTNER_ID)
                        remove(KEY_PAIR_ID)
                        remove(KEY_IS_PAIRED)
                        apply()
                    }
                    _photos.value = emptyList()
                    clearCommentIdentity()
                    _loading.value = false
                    return@launch
                }

                prefs.edit().apply {
                    putBoolean(KEY_IS_PAIRED, true)
                    putString(KEY_PARTNER_ID, partnerId)
                    putString(KEY_PAIR_ID, pair?.pairId)
                    apply()
                }

                updateCommentIdentity(myId, partnerId)

                val since = if (_filterDays.value > 0) {
                    System.currentTimeMillis() - _filterDays.value * 86400000L
                } else {
                    0L
                }
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
                _error.value = "上传失败：${e.localizedMessage ?: "请稍后再试"}"
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

    fun preloadCommentCount(photo: AlbumPhoto) {
        if (photo.id <= 0L || _commentCounts.value.containsKey(photo.id)) return
        viewModelScope.launch {
            runCatching { albumCommentRepository.getComments(photo.id).size }
                .onSuccess { count ->
                    _commentCounts.value = _commentCounts.value + (photo.id to count)
                }
                .onFailure { Log.e(TAG, "preloadCommentCount failed", it) }
        }
    }

    fun openComments(photo: AlbumPhoto) {
        if (photo.id <= 0L) {
            _commentError.value = "这张照片还在同步，稍后再试试"
            return
        }
        _activeCommentPhotoId.value = photo.id
        _comments.value = emptyList()
        _commentText.value = ""
        _commentError.value = ""
        commentsPollingJob?.cancel()
        commentsPollingJob = viewModelScope.launch {
            _commentsLoading.value = true
            refreshComments(photo.id)
            _commentsLoading.value = false
            while (isActive) {
                delay(COMMENT_POLL_MS)
                refreshComments(photo.id)
            }
        }
    }

    fun closeComments() {
        commentsPollingJob?.cancel()
        commentsPollingJob = null
        _activeCommentPhotoId.value = null
        _comments.value = emptyList()
        _commentText.value = ""
        _commentError.value = ""
    }

    fun updateCommentText(text: String) {
        if (text.length <= AlbumCommentRepository.MAX_CONTENT_LENGTH) {
            _commentText.value = text
        }
    }

    fun sendComment() {
        val photoId = _activeCommentPhotoId.value ?: return
        val content = _commentText.value.trim()
        val authorDeviceId = _myDeviceId.value
        if (content.isEmpty() || authorDeviceId.isEmpty() || _sendingComment.value) return

        viewModelScope.launch {
            _sendingComment.value = true
            _commentError.value = ""
            try {
                albumCommentRepository.addComment(photoId, authorDeviceId, content)
                _commentText.value = ""
                refreshComments(photoId)
            } catch (e: Exception) {
                Log.e(TAG, "sendComment failed", e)
                _commentError.value = e.message ?: "评论发送失败，请稍后重试"
            } finally {
                _sendingComment.value = false
            }
        }
    }

    fun deleteComment(comment: AlbumComment) {
        if (comment.authorDeviceId != _myDeviceId.value || _deletingCommentId.value != null) return
        viewModelScope.launch {
            _deletingCommentId.value = comment.id
            _commentError.value = ""
            try {
                albumCommentRepository.deleteComment(comment.id, _myDeviceId.value)
                _comments.value = _comments.value.filterNot { it.id == comment.id }
                _commentCounts.value = _commentCounts.value + (comment.photoId to _comments.value.size)
            } catch (e: Exception) {
                Log.e(TAG, "deleteComment failed", e)
                _commentError.value = e.message ?: "删除评论失败，请稍后重试"
            } finally {
                _deletingCommentId.value = null
            }
        }
    }

    private suspend fun refreshComments(photoId: Long) {
        try {
            val loadedComments = albumCommentRepository.getComments(photoId)
            if (_activeCommentPhotoId.value == photoId) {
                _comments.value = loadedComments
                _commentCounts.value = _commentCounts.value + (photoId to loadedComments.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshComments failed", e)
            if (_activeCommentPhotoId.value == photoId) {
                _commentError.value = "评论加载失败，请稍后重试"
            }
        }
    }

    private fun updateCommentIdentity(myId: String, partnerId: String) {
        _myDeviceId.value = myId
        _myCommentProfile.value = AlbumCommentProfile(
            displayName = prefs.getString(KEY_SELF_NAME, "")?.trim().orEmpty().ifEmpty { DEFAULT_MY_NAME },
            avatarEmoji = prefs.getString(KEY_AVATAR_EMOJI, UserStatus.DEFAULT_AVATAR_EMOJI)
                ?: UserStatus.DEFAULT_AVATAR_EMOJI,
            avatarUrl = prefs.getString(KEY_AVATAR_URL, "") ?: ""
        )
        _partnerCommentProfile.value = AlbumCommentProfile(
            displayName = SettingsDisplayName.resolvePartnerDisplayName(
                nickname = prefs.getString(KEY_PARTNER_NICKNAME, ""),
                partnerName = prefs.getString(KEY_PARTNER_NAME, "")
            )
        )

        if (observedPartnerId == partnerId) return
        observedPartnerId = partnerId
        partnerProfileJob?.cancel()
        partnerProfileJob = viewModelScope.launch {
            statusRepository.observeStatus(partnerId).collect { partnerStatus ->
                partnerStatus ?: return@collect
                _partnerCommentProfile.value = _partnerCommentProfile.value.copy(
                    avatarEmoji = partnerStatus.avatarEmoji.ifEmpty { UserStatus.DEFAULT_AVATAR_EMOJI },
                    avatarUrl = partnerStatus.avatarUrl
                )
            }
        }
    }

    private fun clearCommentIdentity() {
        _myDeviceId.value = ""
        _myCommentProfile.value = AlbumCommentProfile()
        _partnerCommentProfile.value = AlbumCommentProfile(displayName = DEFAULT_PARTNER_NAME)
        observedPartnerId = ""
        partnerProfileJob?.cancel()
        partnerProfileJob = null
        closeComments()
    }

    companion object {
        private const val TAG = "StandByAlbumVM"
        private const val KEY_IS_PAIRED = "is_paired"
        private const val KEY_PARTNER_ID = "partner_id"
        private const val KEY_PAIR_ID = "pair_id"
        private const val KEY_SELF_NAME = "self_name"
        private const val KEY_PARTNER_NAME = "partner_name"
        private const val KEY_PARTNER_NICKNAME = "partner_nickname"
        private const val KEY_AVATAR_EMOJI = "avatar_emoji"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val DEFAULT_MY_NAME = "我"
        private const val DEFAULT_PARTNER_NAME = "对方"
        private const val COMMENT_POLL_MS = 6_000L
    }
}

data class AlbumCommentProfile(
    val displayName: String = "我",
    val avatarEmoji: String = UserStatus.DEFAULT_AVATAR_EMOJI,
    val avatarUrl: String = ""
)
