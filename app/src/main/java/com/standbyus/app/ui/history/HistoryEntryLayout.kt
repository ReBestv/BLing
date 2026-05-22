package com.standbyus.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class HistoryBubbleWidth {
    data object Wrap : HistoryBubbleWidth()
    data class Fixed(val width: Dp) : HistoryBubbleWidth()
}

object HistoryEntryLayout {
    fun horizontalArrangement(isMe: Boolean): Arrangement.Horizontal {
        return if (isMe) Arrangement.End else Arrangement.Start
    }

    fun avatarGap(isMe: Boolean): Dp {
        return if (isMe) 4.dp else 12.dp
    }

    fun bubbleWidth(isMe: Boolean): HistoryBubbleWidth {
        return HistoryBubbleWidth.Fixed(260.dp)
    }

    fun bubbleContentAlignment(isMe: Boolean): Alignment.Horizontal {
        return if (isMe) Alignment.End else Alignment.Start
    }

    fun cardContentArrangement(isMe: Boolean): Arrangement.Horizontal {
        return if (isMe) Arrangement.End else Arrangement.Start
    }

    fun cardMinHeight(): Dp = 72.dp

    fun cardHorizontalPadding(): Dp = 12.dp

    fun cardVerticalPadding(): Dp = 8.dp

    fun moodBadgeSize(): Dp = 56.dp

    fun moodImageSize(): Dp = 40.dp

    fun moodToTextGap(): Dp = 14.dp
}
