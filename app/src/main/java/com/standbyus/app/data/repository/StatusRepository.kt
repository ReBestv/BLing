package com.standbyus.app.data.repository

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.standbyus.app.data.local.StatusDao
import com.standbyus.app.data.local.toEntity
import com.standbyus.app.data.local.toUserStatus
import com.standbyus.app.data.model.UserStatus
import com.standbyus.app.data.remote.SupabaseService
import com.standbyus.app.widget.StandByWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusRepository @Inject constructor(
    private val supabaseService: SupabaseService,
    private val statusDao: StatusDao,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "StandByStatusRepo"
        private const val TABLE = "statuses"
        private const val POLL_MS = 6_000L
    }

    suspend fun updateStatus(userStatus: UserStatus) {
        Log.d(TAG, "Updating status for user: ${userStatus.userId}")
        try {
            // 时光轴：每次发布都向数据库插入新记录，而不是覆盖旧记录
            val data = userStatus.toMap()
            supabaseService.create(TABLE, data)
            Log.d(TAG, "Supabase create success (append to history)")
        } catch (e: Exception) {
            Log.e(TAG, "Supabase write failed: ${e.message}", e)
            throw e
        }
        // Room 缓存
        statusDao.upsertStatus(userStatus.toEntity())
        // Widget 缓存
    }

    suspend fun updateAvatarSnapshot(userId: String, avatarEmoji: String, avatarUrl: String) {
        if (userId.isEmpty()) return
        supabaseService.update(
            TABLE,
            "userId=eq.$userId",
            mapOf(
                "avatarEmoji" to avatarEmoji,
                "avatarUrl" to avatarUrl
            )
        )
    }

    private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun updateWidgetCache(status: UserStatus) {
        val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("doing", status.doing)
            putString("customDoing", status.customDoing)
            putString("themeId", status.themeId)
            putString("themeName", status.themeName)
            putString("feelingKey", status.feelingKey)
            putString("feelingLabel", status.feelingLabel)
            putString("feelingAsset", status.feelingAsset)
            putString("feelingFallbackEmoji", status.feelingFallbackEmoji)
            putString("feelingColor", status.feelingColor)
            putString("stickerId", status.stickerId)
            putString("stickerLabel", status.stickerLabel)
            putString("stickerAsset", status.stickerAsset)
            putString("avatarEmoji", status.avatarEmoji)
            putString("avatarUrl", status.avatarUrl)
            putString("note", status.note)
            putLong("updatedAt", status.updatedAt)
            apply()
        }
        widgetScope.launch {
            StandByWidget().updateAll(context)
        }
    }

    suspend fun getCachedStatus(userId: String): UserStatus? {
        return statusDao.getStatus(userId)?.toUserStatus()
    }

    /** 轮询方式观察对方状态 */
    fun observeStatus(userId: String): Flow<UserStatus?> = callbackFlow {
        Log.d(TAG, "observeStatus start for $userId")

        // 立即从网络获取最新状态
        suspend fun fetchFromRemote() {
            try {
                val results = supabaseService.query(
                    TABLE,
                    "userId=eq.$userId&order=updatedAt.desc&limit=1"
                )
                if (results.isNotEmpty()) {
                    val status = UserStatus.fromMap(results[0])
                    trySend(status)
                    statusDao.upsertStatus(status.toEntity())
                } else {
                    Log.d(TAG, "observeStatus no remote data for $userId")
                    val cached = statusDao.getStatus(userId)?.toUserStatus()
                    if (cached != null) trySend(cached)
                }
            } catch (e: Exception) {
                Log.e(TAG, "observeStatus remote failed: ${e.message}")
                val cached = statusDao.getStatus(userId)?.toUserStatus()
                if (cached != null) trySend(cached)
            }
        }

        // 首次立即执行
        fetchFromRemote()

        // 然后定时轮询
        val job = widgetScope.launch {
            delay(POLL_MS) // 等一轮再查
            while (isActive) {
                fetchFromRemote()
                delay(POLL_MS)
            }
        }
        awaitClose { job.cancel() }
    }
}
