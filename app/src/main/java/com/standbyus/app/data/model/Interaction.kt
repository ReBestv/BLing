package com.standbyus.app.data.model

enum class InteractionType(
    val key: String,
    val sendText: String
) {
    HUG("hug", "抱抱一个"),
    MISS_YOU_TOO("miss_you_too", "我也想你"),
    HARD_WORK("hard_work", "辛苦啦"),
    NUDGE_UPDATE("nudge_update", "不宁不宁在干嘛"),
    POOP_CHECKIN("poop_checkin", "正在拉屎");

    fun receivedText(senderName: String = "TA"): String = when (this) {
        HUG -> "${senderName}抱了你一个"
        MISS_YOU_TOO -> "${senderName}也想你了"
        HARD_WORK -> "${senderName}说你辛苦啦"
        NUDGE_UPDATE -> "${senderName}不宁不宁在干嘛"
        POOP_CHECKIN -> "${senderName}正在拉屎"
    }

    companion object {
        fun fromKey(key: String): InteractionType? = entries.find { it.key == key }
    }
}

data class Interaction(
    val id: Long = 0L,
    val fromUserId: String = "",
    val toUserId: String = "",
    val type: String = "",
    val text: String = "",
    val targetStatusTime: Long = 0L,
    val createdAt: Long = 0L,
    val readAt: Long? = null
) {
    fun toMap(): Map<String, Any> = buildMap {
        put("fromUserId", fromUserId)
        put("toUserId", toUserId)
        put("type", type)
        put("text", text)
        put("targetStatusTime", targetStatusTime)
        put("createdAt", createdAt)
        readAt?.let { put("readAt", it) }
    }

    fun displayText(senderName: String = "TA"): String {
        return InteractionType.fromKey(type)?.receivedText(senderName)
            ?: text.ifEmpty { "${senderName}回应了你" }
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Interaction {
            return Interaction(
                id = (map["id"] as? Number)?.toLong() ?: 0L,
                fromUserId = map["fromUserId"] as? String ?: "",
                toUserId = map["toUserId"] as? String ?: "",
                type = map["type"] as? String ?: "",
                text = map["text"] as? String ?: "",
                targetStatusTime = (map["targetStatusTime"] as? Number)?.toLong() ?: 0L,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                readAt = (map["readAt"] as? Number)?.toLong()
            )
        }
    }
}
