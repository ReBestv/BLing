package com.standbyus.app.ui.settings

data class SettingsLayoutMetrics(
    val horizontalPaddingDp: Float,
    val cardPaddingDp: Float,
    val profileAvatarSizeDp: Float,
    val avatarPickerMaxHeightDp: Float
)

object SettingsLayout {
    fun metrics(
        availableWidthDp: Float,
        availableHeightDp: Float
    ): SettingsLayoutMetrics {
        val compactWidth = availableWidthDp < 360f
        val compactHeight = availableHeightDp < 620f
        val compact = compactWidth || compactHeight

        return SettingsLayoutMetrics(
            horizontalPaddingDp = if (compactWidth) 12f else 16f,
            cardPaddingDp = if (compact) 16f else 20f,
            profileAvatarSizeDp = if (compact) 56f else 64f,
            avatarPickerMaxHeightDp = if (compact) 320f else 400f
        )
    }
}
