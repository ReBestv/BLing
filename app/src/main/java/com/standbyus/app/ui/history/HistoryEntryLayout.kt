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
        return if (isMe) HistoryBubbleWidth.Wrap else HistoryBubbleWidth.Fixed(260.dp)
    }

    fun bubbleContentAlignment(isMe: Boolean): Alignment.Horizontal {
        return if (isMe) Alignment.End else Alignment.Start
    }
}
