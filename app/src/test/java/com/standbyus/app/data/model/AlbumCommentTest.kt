package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AlbumCommentTest {

    @Test
    fun `maps Supabase comment payload`() {
        val comment = AlbumComment.fromMap(
            mapOf(
                "id" to 17L,
                "photoId" to 9L,
                "authorDeviceId" to "device-a",
                "content" to "今天的云好漂亮",
                "createdAt" to 1_725_000_000_000L
            )
        )

        assertEquals(17L, comment.id)
        assertEquals(9L, comment.photoId)
        assertEquals("device-a", comment.authorDeviceId)
        assertEquals("今天的云好漂亮", comment.content)
        assertEquals(1_725_000_000_000L, comment.createdAt)
    }

    @Test
    fun `uses safe defaults for incomplete payload`() {
        val comment = AlbumComment.fromMap(emptyMap())

        assertEquals(0L, comment.id)
        assertEquals(0L, comment.photoId)
        assertEquals("", comment.authorDeviceId)
        assertEquals("", comment.content)
        assertEquals(0L, comment.createdAt)
    }
}
