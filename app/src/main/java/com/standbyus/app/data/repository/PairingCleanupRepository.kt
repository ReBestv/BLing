package com.standbyus.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.standbyus.app.data.local.CheckinDao
import com.standbyus.app.data.local.StatusDao
import com.standbyus.app.data.local.TodoItemDao
import com.standbyus.app.data.local.TodoListDao
import com.standbyus.app.data.model.PairingInfo
import com.standbyus.app.data.remote.SupabaseService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingCleanupRepository @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val supabaseService: SupabaseService,
    private val statusDao: StatusDao,
    private val checkinDao: CheckinDao,
    private val todoListDao: TodoListDao,
    private val todoItemDao: TodoItemDao,
    @ApplicationContext private val context: Context
) {
    suspend fun unpairAndClearData(userId: String): CleanupResult {
        val failures = mutableListOf<String>()
        val pair = runCatching { pairingRepository.findPairByUserId(userId) }
            .onFailure {
                Log.e(TAG, "findPairByUserId failed", it)
                failures += "find_pair"
            }
            .getOrNull()

        val partnerId = pair.partnerIdFor(userId)
        val userIds = listOf(userId, partnerId).filter { it.isNotEmpty() }.distinct()

        if (!pair?.pairId.isNullOrEmpty()) {
            runCleanup("todo_remote", failures) {
                clearRemoteTodos(pair!!.pairId)
            }
        }

        userIds.forEach { targetUserId ->
            runCleanup("status_remote_$targetUserId", failures) {
                supabaseService.delete(TABLE_STATUSES, "userId=eq.$targetUserId")
            }
            runCleanup("checkin_remote_$targetUserId", failures) {
                supabaseService.delete(TABLE_CHECKINS, "userId=eq.$targetUserId")
            }
            runCleanup("photo_remote_$targetUserId", failures) {
                clearRemotePhotos(targetUserId)
            }
        }

        if (userIds.size == 2) {
            val first = userIds[0]
            val second = userIds[1]
            runCleanup("interaction_remote_pair", failures) {
                supabaseService.delete(
                    TABLE_INTERACTIONS,
                    "or=(and(fromUserId.eq.$first,toUserId.eq.$second),and(fromUserId.eq.$second,toUserId.eq.$first))"
                )
            }
        } else {
            val onlyUserId = userIds.firstOrNull()
            if (!onlyUserId.isNullOrEmpty()) {
                runCleanup("interaction_remote_single", failures) {
                    supabaseService.delete(TABLE_INTERACTIONS, "toUserId=eq.$onlyUserId")
                }
            }
        }

        if (!pair?.pairId.isNullOrEmpty()) {
            runCleanup("pair_remote", failures) {
                supabaseService.delete(TABLE_PAIRS, "pairId=eq.${pair!!.pairId}")
            }
        }

        runCleanup("status_local", failures) {
            statusDao.clearAll()
        }
        userIds.forEach { targetUserId ->
            runCleanup("checkin_local_$targetUserId", failures) {
                checkinDao.clearUserRecords(targetUserId)
            }
        }
        runCleanup("todo_local_items", failures) {
            todoItemDao.clearAll()
        }
        runCleanup("todo_local_lists", failures) {
            todoListDao.clearAll()
        }
        runCleanup("widget_cache", failures) {
            context.getSharedPreferences(WIDGET_CACHE_PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }

        return CleanupResult(hadFailures = failures.isNotEmpty())
    }

    suspend fun clearLocalRelationshipData(
        userId: String,
        partnerId: String? = null
    ) {
        val userIds = listOf(userId, partnerId).mapNotNull { it?.takeIf(String::isNotEmpty) }.distinct()
        statusDao.clearAll()
        userIds.forEach { targetUserId ->
            checkinDao.clearUserRecords(targetUserId)
        }
        todoItemDao.clearAll()
        todoListDao.clearAll()
        context.getSharedPreferences(WIDGET_CACHE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    private suspend fun clearRemoteTodos(pairId: String) {
        val lists = supabaseService.query(
            TABLE_TODO_LISTS,
            "pair_id=eq.$pairId&select=id"
        )
        lists.mapNotNull { (it["id"] as? Number)?.toLong() }
            .forEach { listId ->
                supabaseService.delete(TABLE_TODO_ITEMS, "list_id=eq.$listId")
            }
        supabaseService.delete(TABLE_TODO_LISTS, "pair_id=eq.$pairId")
    }

    private suspend fun clearRemotePhotos(userId: String) {
        val photos = supabaseService.query(
            TABLE_PHOTOS,
            "deviceId=eq.$userId&select=id,url"
        )
        photos.forEach { photo ->
            val url = photo["url"] as? String ?: return@forEach
            val path = Uri.parse(url).path
                ?.substringAfter("/object/public/$PHOTO_BUCKET/", "")
                ?.takeIf { it.isNotEmpty() }
                ?: return@forEach
            runCatching {
                supabaseService.deleteFile(PHOTO_BUCKET, path)
            }.onFailure {
                Log.e(TAG, "deleteFile failed for $path", it)
            }
        }
        supabaseService.delete(TABLE_PHOTOS, "deviceId=eq.$userId")
    }

    private suspend fun runCleanup(
        label: String,
        failures: MutableList<String>,
        action: suspend () -> Unit
    ) {
        runCatching { action() }.onFailure {
            Log.e(TAG, "Cleanup step failed: $label", it)
            failures += label
        }
    }

    private fun PairingInfo?.partnerIdFor(userId: String): String {
        val pair = this ?: return ""
        return when {
            pair.user1Id == userId -> pair.user2Id
            pair.user2Id == userId -> pair.user1Id
            else -> ""
        }
    }

    data class CleanupResult(val hadFailures: Boolean)

    companion object {
        private const val TAG = "PairingCleanupRepo"
        private const val TABLE_PAIRS = "pairs"
        private const val TABLE_STATUSES = "statuses"
        private const val TABLE_CHECKINS = "checkins"
        private const val TABLE_INTERACTIONS = "interactions"
        private const val TABLE_PHOTOS = "photos"
        private const val TABLE_TODO_LISTS = "todo_lists"
        private const val TABLE_TODO_ITEMS = "todo_items"
        private const val PHOTO_BUCKET = "our-story"
        private const val WIDGET_CACHE_PREFS = "widget_cache"
    }
}
