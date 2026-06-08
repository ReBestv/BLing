package com.standbyus.app.data.model

data class TodoItem(
    val id: Long = 0,
    val listId: Long = 0,
    val title: String = "",
    val isDone: Boolean = false,
    val note: String = "",
    val ownerId: String = "",
    val doneBy: String? = null,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val doneAt: Long? = null
) {
    fun toMap(): Map<String, Any> = buildMap {
        if (id > 0) put("id", id)
        put("list_id", listId)
        put("title", title)
        put("is_done", isDone)
        put("note", note)
        put("owner_id", ownerId)
        doneBy?.let { put("done_by", it) }
        put("sort_order", sortOrder)
        put("created_at", createdAt)
        doneAt?.let { put("done_at", it) }
    }

    companion object {
        fun fromMap(map: Map<String, Any>): TodoItem = TodoItem(
            id = (map["id"] as? Number)?.toLong() ?: 0,
            listId = (map["list_id"] as? Number)?.toLong() ?: 0,
            title = map["title"] as? String ?: "",
            isDone = map["is_done"] as? Boolean ?: false,
            note = map["note"] as? String ?: "",
            ownerId = map["owner_id"] as? String ?: "",
            doneBy = map["done_by"] as? String,
            sortOrder = (map["sort_order"] as? Number)?.toLong() ?: 0,
            createdAt = (map["created_at"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            doneAt = (map["done_at"] as? Number)?.toLong()
        )
    }
}
