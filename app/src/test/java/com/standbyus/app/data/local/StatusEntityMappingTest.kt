package com.standbyus.app.data.local

import com.standbyus.app.data.model.UserStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusEntityMappingTest {

    @Test
    fun `preserves status snapshot fields through local cache mapping`() {
        val status = UserStatus(
            userId = "user-a",
            doing = "吃饭",
            customDoing = "刚刚忙完",
            themeId = "emoji_motion",
            themeName = "动态表情",
            feelingKey = "happy",
            feelingLabel = "开心",
            feelingAsset = "https://example.com/happy.webp",
            feelingFallbackEmoji = "😊",
            feelingColor = "#FFFFD180",
            stickerId = "eating",
            stickerLabel = "干饭",
            stickerAsset = "https://example.com/eating.webp",
            avatarEmoji = "🥰",
            avatarUrl = "https://example.com/avatars/user-a.jpg",
            note = "晚点找你",
            updatedAt = 123456789L,
            source = "manual"
        )

        val restored = status.toEntity().toUserStatus()

        assertEquals(status, restored)
    }
}
