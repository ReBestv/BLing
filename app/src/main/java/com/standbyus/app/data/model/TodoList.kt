package com.standbyus.app.data.model

data class TodoList(
    val id: Long = 0,
    val pairId: String = "",
    val name: String = "",
    val ownerId: String = "",
    val isShared: Boolean = false,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = buildMap {
        if (id > 0) put("id", id)
        put("pair_id", pairId)
        put("name", name)
        put("owner_id", ownerId)
        put("is_shared", isShared)
        put("sort_order", sortOrder)
        put("created_at", createdAt)
    }

    companion object {
        fun fromMap(map: Map<String, Any>): TodoList = TodoList(
            id = (map["id"] as? Number)?.toLong() ?: 0,
            pairId = map["pair_id"] as? String ?: "",
            name = map["name"] as? String ?: "",
            ownerId = map["owner_id"] as? String ?: "",
            isShared = map["is_shared"] as? Boolean ?: false,
            sortOrder = (map["sort_order"] as? Number)?.toLong() ?: 0,
            createdAt = (map["created_at"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}
