package com.standbyus.app.data.model

data class CheckinData(
    val id: Long = 0,
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "timestamp" to timestamp,
        "note" to note
    )

    companion object {
        fun fromMap(map: Map<String, Any>): CheckinData {
            return CheckinData(
                id = (map["id"] as? Number)?.toLong() ?: 0,
                userId = map["userId"] as? String ?: "",
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0,
                note = map["note"] as? String ?: ""
            )
        }
    }
}
