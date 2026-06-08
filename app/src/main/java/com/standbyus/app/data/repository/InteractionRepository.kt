package com.standbyus.app.data.repository

import android.util.Log
import com.standbyus.app.data.model.Interaction
import com.standbyus.app.data.model.InteractionType
import com.standbyus.app.data.remote.SupabaseService
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class InteractionRepository @Inject constructor(
    private val supabaseService: SupabaseService
) {
    private val pollScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun sendInteraction(
        fromUserId: String,
        toUserId: String,
        type: InteractionType,
        targetStatusTime: Long
    ): Interaction? {
        val interaction = Interaction(
            fromUserId = fromUserId,
            toUserId = toUserId,
            type = type.key,
            text = type.sendText,
            targetStatusTime = targetStatusTime,
            createdAt = System.currentTimeMillis()
        )
        return supabaseService.create(TABLE, interaction.toMap())?.let(Interaction::fromMap)
    }

    suspend fun getLatestReceivedInteraction(
        myUserId: String,
        partnerUserId: String? = null
    ): Interaction? {
        val query = buildString {
            append("toUserId=eq.$myUserId")
            if (!partnerUserId.isNullOrEmpty()) {
                append("&fromUserId=eq.$partnerUserId")
            }
            append("&order=createdAt.desc&limit=1")
        }
        val results = supabaseService.query(TABLE, query)
        return results.firstOrNull()?.let(Interaction::fromMap)
    }

    suspend fun markRead(interactionId: Long) {
        if (interactionId <= 0L) return
        supabaseService.update(
            TABLE,
            "id=eq.$interactionId",
            mapOf("readAt" to System.currentTimeMillis())
        )
    }

    fun observeLatestReceivedInteraction(
        myUserId: String,
        partnerUserId: String? = null
    ): Flow<Interaction?> = callbackFlow {
        suspend fun fetchLatest() {
            try {
                trySend(getLatestReceivedInteraction(myUserId, partnerUserId))
            } catch (e: Exception) {
                Log.e(TAG, "latest interaction query failed: ${e.message}")
            }
        }

        fetchLatest()
        val job = pollScope.launch {
            delay(POLL_MS)
            while (isActive) {
                fetchLatest()
                delay(POLL_MS)
            }
        }
        awaitClose { job.cancel() }
    }

    companion object {
        private const val TAG = "StandByInteractionRepo"
        private const val TABLE = "interactions"
        private const val POLL_MS = 6_000L
    }
}
