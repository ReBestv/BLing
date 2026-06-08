package com.standbyus.app.data.model

data class AlbumPhoto(
    val id: Long = 0,
    val deviceId: String = "",
    val url: String = "",
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMap(map: Map<String, Any>): AlbumPhoto = AlbumPhoto(
            id = (map["id"] as? Number)?.toLong() ?: 0,
            deviceId = map["deviceId"] as? String ?: "",
            url = map["url"] as? String ?: "",
            caption = map["caption"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
