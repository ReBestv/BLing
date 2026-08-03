package com.standbyus.app.data.model

data class AlbumComment(
    val id: Long = 0L,
    val photoId: Long = 0L,
    val authorDeviceId: String = "",
    val content: String = "",
    val createdAt: Long = 0L
) {
    companion object {
        fun fromMap(map: Map<String, Any>): AlbumComment = AlbumComment(
            id = (map["id"] as? Number)?.toLong() ?: 0L,
            photoId = (map["photoId"] as? Number)?.toLong() ?: 0L,
            authorDeviceId = map["authorDeviceId"] as? String ?: "",
            content = map["content"] as? String ?: "",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }
}
