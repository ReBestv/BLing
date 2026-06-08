package com.standbyus.app.data.repository

import android.util.Log
import com.standbyus.app.data.model.PairingInfo
import com.standbyus.app.data.remote.SupabaseService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepository @Inject constructor(
    private val supabaseService: SupabaseService
) {
    companion object {
        private const val TAG = "StandByPairing"
        private const val TABLE = "pairs"
    }

    suspend fun createPairingCode(creatorUserId: String, creatorName: String): String {
        cleanupPairsForUser(creatorUserId)

        val pairId = generatePairingCode()
        Log.d(TAG, "Creating pairing code: $pairId for user $creatorUserId")
        val data = mapOf(
            "pairId" to pairId,
            "user1Id" to creatorUserId,
            "user2Id" to "",
            "user1Name" to creatorName,
            "user2Name" to ""
        )
        try {
            supabaseService.create(TABLE, data)
            Log.d(TAG, "Pairing code created successfully: $pairId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create pairing code: ${e.message}", e)
            throw e
        }
        return pairId
    }

    data class JoinResult(
        val success: Boolean,
        val partnerId: String = "",
        val pairInfo: PairingInfo? = null
    )

    suspend fun joinPair(pairId: String, userId: String, joinerName: String): JoinResult {
        cleanupPairsForUser(userId)

        val normalizedPairId = pairId.trim().uppercase()
        val pair = getPairInfo(normalizedPairId) ?: return JoinResult(false)

        val updatedPair = when {
            pair.user1Id == userId || pair.user2Id == userId -> pair
            pair.user1Id.isEmpty() -> {
                supabaseService.update(
                    TABLE,
                    "pairId=eq.$normalizedPairId",
                    mapOf("user1Id" to userId, "user1Name" to joinerName)
                )
                getPairInfo(normalizedPairId)
            }
            pair.user2Id.isEmpty() -> {
                supabaseService.update(
                    TABLE,
                    "pairId=eq.$normalizedPairId",
                    mapOf("user2Id" to userId, "user2Name" to joinerName)
                )
                getPairInfo(normalizedPairId)
            }
            else -> null
        } ?: return JoinResult(false)

        val partnerId = updatedPair.partnerIdFor(userId)
        return JoinResult(
            success = partnerId.isNotEmpty(),
            partnerId = partnerId,
            pairInfo = updatedPair
        )
    }

    suspend fun getPairInfo(pairId: String): PairingInfo? {
        val normalizedPairId = pairId.trim().uppercase()
        val results = supabaseService.query(TABLE, "pairId=eq.$normalizedPairId&limit=1")
        if (results.isEmpty()) return null
        return PairingInfo.fromMap(results[0])
    }

    suspend fun findPairByUserId(userId: String): PairingInfo? {
        val results = supabaseService.query(
            TABLE,
            "or=(user1Id.eq.$userId,user2Id.eq.$userId)"
        )
        val pairs = results.map(PairingInfo::fromMap)
        return pairs.firstOrNull { it.user1Id.isNotEmpty() && it.user2Id.isNotEmpty() }
            ?: pairs.firstOrNull()
    }

    suspend fun unpair(userId: String) {
        cleanupPairsForUser(userId)
    }

    private suspend fun cleanupPairsForUser(userId: String) {
        if (userId.isEmpty()) return
        runCatching {
            supabaseService.delete(TABLE, "or=(user1Id.eq.$userId,user2Id.eq.$userId)")
        }.onFailure {
            Log.e(TAG, "cleanupPairsForUser failed: ${it.message}", it)
        }
    }

    private fun PairingInfo.partnerIdFor(userId: String): String {
        return when {
            user1Id == userId -> user2Id
            user2Id == userId -> user1Id
            else -> ""
        }
    }

    private fun generatePairingCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
