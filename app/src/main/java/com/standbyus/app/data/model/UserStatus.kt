package com.standbyus.app.data.model

data class UserStatus(
    val userId: String = "",
    val doing: String = "",
    val customDoing: String = "",
    val feeling: String = "开心",
    val feelingColor: String = "#FFD93D",
    val feelingEmoji: String = "😊",
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val source: String = "manual"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "doing" to doing,
        "customDoing" to customDoing,
        "feeling" to feeling,
        "feelingColor" to feelingColor,
        "feelingEmoji" to feelingEmoji,
        "note" to note,
        "updatedAt" to updatedAt,
        "source" to source
    )

    companion object {
        fun fromMap(map: Map<String, Any>): UserStatus = UserStatus(
            userId = map["userId"] as? String ?: "",
            doing = map["doing"] as? String ?: "",
            customDoing = map["customDoing"] as? String ?: "",
            feeling = map["feeling"] as? String ?: "开心",
            feelingColor = map["feelingColor"] as? String ?: "#FFD93D",
            feelingEmoji = map["feelingEmoji"] as? String ?: "😊",
            note = map["note"] as? String ?: "",
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            source = map["source"] as? String ?: "manual"
        )
    }
}
