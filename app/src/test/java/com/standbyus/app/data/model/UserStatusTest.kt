package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UserStatusTest {

    @Test
    fun readsLegacyChineseFeelingIntoKeyedSnapshot() {
        val status = UserStatus.fromMap(
            mapOf(
                "feeling" to "沮丧",
                "feelingEmoji" to "😞",
                "feelingColor" to "#D7CCC8"
            )
        )

        assertEquals("upset", status.feelingKey)
        assertEquals("沮丧", status.feelingLabel)
        assertEquals("😞", status.feelingAsset)
        assertEquals("😞", status.feelingFallbackEmoji)
        assertEquals("#D7CCC8", status.feelingColor)
    }

    @Test
    fun writesOnlySnapshotMoodFieldsForNewRows() {
        val status = UserStatus(
            feelingKey = "upset",
            feelingLabel = "沮丧",
            feelingAsset = "https://example.com/upset.png",
            feelingFallbackEmoji = "😞",
            feelingColor = "#D7CCC8"
        )

        val map = status.toMap()

        assertEquals("upset", map["feelingKey"])
        assertEquals("沮丧", map["feelingLabel"])
        assertEquals("https://example.com/upset.png", map["feelingAsset"])
        assertFalse(map.containsKey("feeling"))
        assertFalse(map.containsKey("feelingEmoji"))
    }

    @Test
    fun writesAndReadsOptionalStickerFields() {
        val status = UserStatus(
            feelingKey = "happy",
            feelingLabel = "开心",
            feelingAsset = "https://example.com/themes/VV/eating.webp",
            feelingFallbackEmoji = "😊",
            stickerId = "eating",
            stickerLabel = "吃饭",
            stickerAsset = "https://example.com/themes/VV/eating.webp"
        )

        val map = status.toMap()
        val restored = UserStatus.fromMap(map)

        assertEquals("eating", map["stickerId"])
        assertEquals("吃饭", map["stickerLabel"])
        assertEquals("https://example.com/themes/VV/eating.webp", map["stickerAsset"])
        assertEquals("eating", restored.stickerId)
        assertEquals("吃饭", restored.stickerLabel)
        assertEquals("https://example.com/themes/VV/eating.webp", restored.stickerAsset)
    }

    @Test
    fun writesAndReadsAvatarSnapshotFields() {
        val status = UserStatus(
            avatarEmoji = "🥰",
            avatarUrl = "https://example.com/avatars/me.jpg"
        )

        val map = status.toMap()
        val restored = UserStatus.fromMap(map)

        assertEquals("🥰", map["avatarEmoji"])
        assertEquals("https://example.com/avatars/me.jpg", map["avatarUrl"])
        assertEquals("🥰", restored.avatarEmoji)
        assertEquals("https://example.com/avatars/me.jpg", restored.avatarUrl)
    }
}
