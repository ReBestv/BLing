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

    data class JoinResult(val success: Boolean, val partnerId: String = "")

    suspend fun joinPair(pairId: String, userId: String, joinerName: String): JoinResult {
        val results = supabaseService.query(
            TABLE,
            "pairId=eq.$pairId&limit=1"
        )
        if (results.isEmpty()) return JoinResult(false)

        val obj = results[0]
        val existingUser1 = obj["user1Id"] as? String ?: ""
        val existingUser2 = obj["user2Id"] as? String ?: ""

        return if (existingUser1.isEmpty()) {
            supabaseService.update(TABLE, "pairId=eq.$pairId", mapOf("user1Id" to userId, "user1Name" to joinerName))
            JoinResult(true, "")
        } else if (existingUser2.isEmpty()) {
            supabaseService.update(TABLE, "pairId=eq.$pairId", mapOf("user2Id" to userId, "user2Name" to joinerName))
            JoinResult(true, existingUser1)
        } else {
            JoinResult(false)
        }
    }

    suspend fun getPairInfo(pairId: String): PairingInfo? {
        val results = supabaseService.query(TABLE, "pairId=eq.$pairId&limit=1")
        if (results.isEmpty()) return null
        return PairingInfo.fromMap(results[0])
    }

    /** 查询当前用户所属的配对信息 */
    suspend fun findPairByUserId(userId: String): PairingInfo? {
        val results = supabaseService.query(
            TABLE,
            "or=(user1Id.eq.$userId,user2Id.eq.$userId)&limit=1"
        )
        return if (results.isNotEmpty()) PairingInfo.fromMap(results[0]) else null
    }

    /** 解除配对：删除配对记录 */
    suspend fun unpair(userId: String) {
        // 找到该用户所属的配对记录并删除
        val pair = findPairByUserId(userId)
        if (pair != null) {
            supabaseService.delete(TABLE, "pairId=eq.${pair.pairId}")
        }
    }

    private fun generatePairingCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
