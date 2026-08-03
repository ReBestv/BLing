package com.standbyus.app.data.repository

import com.standbyus.app.data.model.AlbumComment
import com.standbyus.app.data.remote.SupabaseService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumCommentRepository @Inject constructor(
    private val supabaseService: SupabaseService
) {
    suspend fun getComments(photoId: Long): List<AlbumComment> {
        if (photoId <= 0L) return emptyList()
        return supabaseService.query(
            TABLE,
            "photoId=eq.$photoId&order=createdAt.asc&limit=$MAX_COMMENTS"
        ).mapNotNull { raw ->
            runCatching { AlbumComment.fromMap(raw) }.getOrNull()
        }
    }

    suspend fun addComment(
        photoId: Long,
        authorDeviceId: String,
        content: String
    ): AlbumComment {
        val normalizedContent = content.trim()
        require(photoId > 0L) { "照片不存在，暂时无法评论" }
        require(authorDeviceId.isNotBlank()) { "设备身份尚未准备好" }
        require(normalizedContent.isNotEmpty()) { "评论不能为空" }
        require(normalizedContent.length <= MAX_CONTENT_LENGTH) { "评论不能超过 $MAX_CONTENT_LENGTH 个字" }

        val createdAt = System.currentTimeMillis()
        val created = supabaseService.create(
            TABLE,
            mapOf(
                "photoId" to photoId,
                "authorDeviceId" to authorDeviceId,
                "content" to normalizedContent,
                "createdAt" to createdAt
            )
        ) ?: throw IOException("评论未保存，请稍后重试")
        return AlbumComment.fromMap(created)
    }

    suspend fun deleteComment(commentId: Long, authorDeviceId: String) {
        if (commentId <= 0L || authorDeviceId.isBlank()) return
        supabaseService.delete(
            TABLE,
            "id=eq.$commentId&authorDeviceId=eq.$authorDeviceId"
        )
    }

    companion object {
        const val MAX_CONTENT_LENGTH = 100
        private const val MAX_COMMENTS = 100
        private const val TABLE = "photo_comments"
    }
}
