package com.standbyus.app.data.model

data class PairingInfo(
    val pairId: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "pairId" to pairId,
        "user1Id" to user1Id,
        "user2Id" to user2Id,
        "user1Name" to user1Name,
        "user2Name" to user2Name
    )

    companion object {
        fun fromMap(map: Map<String, Any>): PairingInfo = PairingInfo(
            pairId = map["pairId"] as? String ?: "",
            user1Id = map["user1Id"] as? String ?: "",
            user2Id = map["user2Id"] as? String ?: "",
            user1Name = map["user1Name"] as? String ?: "",
            user2Name = map["user2Name"] as? String ?: ""
        )
    }
}
