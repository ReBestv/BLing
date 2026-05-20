package com.standbyus.app.data.model

data class StatusFeelingSnapshot(
    val themeId: String = "default",
    val themeName: String = "默认表情",
    val feelingKey: String = Feeling.HAPPY.key,
    val feelingLabel: String = Feeling.HAPPY.displayName,
    val feelingAsset: String = Feeling.HAPPY.emoji,
    val feelingFallbackEmoji: String = Feeling.HAPPY.emoji,
    val feelingColor: String = Feeling.HAPPY.color.toHex()
)

data class UserStatus(
    val userId: String = "",
    val doing: String = "",
    val customDoing: String = "",
    val themeId: String = "default",
    val themeName: String = "默认表情",
    val feelingKey: String = Feeling.HAPPY.key,
    val feelingLabel: String = Feeling.HAPPY.displayName,
    val feelingAsset: String = Feeling.HAPPY.emoji,
    val feelingFallbackEmoji: String = Feeling.HAPPY.emoji,
    val feelingColor: String = Feeling.HAPPY.color.toHex(),
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val source: String = "manual"
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "doing" to doing,
        "customDoing" to customDoing,
        "themeId" to themeId,
        "themeName" to themeName,
        "feelingKey" to feelingKey,
        "feelingLabel" to feelingLabel,
        "feelingAsset" to feelingAsset,
        "feelingFallbackEmoji" to feelingFallbackEmoji,
        "feelingColor" to feelingColor,
        "note" to note,
        "updatedAt" to updatedAt,
        "source" to source
    )

    companion object {
        fun fromMap(map: Map<String, Any>): UserStatus {
            val legacyLabel = map["feeling"] as? String
            val explicitKey = map["feelingKey"] as? String
            val feeling = explicitKey?.let { Feeling.fromKey(it) }
                ?: legacyLabel?.let { Feeling.fromLegacyLabel(it) }
                ?: Feeling.HAPPY
            val fallbackEmoji = map["feelingFallbackEmoji"] as? String
                ?: map["feelingEmoji"] as? String
                ?: feeling.emoji

            return UserStatus(
                userId = map["userId"] as? String ?: "",
                doing = map["doing"] as? String ?: "",
                customDoing = map["customDoing"] as? String ?: "",
                themeId = map["themeId"] as? String ?: "default",
                themeName = map["themeName"] as? String ?: "默认表情",
                feelingKey = explicitKey ?: feeling.key,
                feelingLabel = map["feelingLabel"] as? String ?: legacyLabel ?: feeling.displayName,
                feelingAsset = map["feelingAsset"] as? String ?: map["feelingEmoji"] as? String ?: fallbackEmoji,
                feelingFallbackEmoji = fallbackEmoji,
                feelingColor = map["feelingColor"] as? String ?: feeling.color.toHex(),
                note = map["note"] as? String ?: "",
                updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                source = map["source"] as? String ?: "manual"
            )
        }
    }
}

fun androidx.compose.ui.graphics.Color.toHex(): String {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return "#${a.toString(16).padStart(2, '0')}${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}"
}
