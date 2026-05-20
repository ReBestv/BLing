package com.standbyus.app.ui.theme

import com.standbyus.app.data.model.ThemePack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiThemeManagerTest {

    @Test
    fun `theme version changes when manifest themes update`() {
        val before = EmojiThemeManager.themeVersion.value

        EmojiThemeManager.updateThemes(
            listOf(
                ThemePack(
                    id = "xiaoxin",
                    name = "小新",
                    bucket = "xiaoxin",
                    icon = "happy.png",
                    feelings = listOf("开心"),
                    fileNameMap = mapOf("开心" to "happy")
                )
            )
        )

        assertTrue(EmojiThemeManager.themeVersion.value > before)
        assertEquals("xiaoxin", EmojiThemeManager.themes.last().id)
    }
}
