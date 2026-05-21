package com.standbyus.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HistoryEntryLayoutTest {

    @Test
    fun `keeps my timeline entry on the right`() {
        assertSame(
            Arrangement.End,
            HistoryEntryLayout.horizontalArrangement(isMe = true)
        )
    }

    @Test
    fun `keeps partner timeline entry unchanged on the left`() {
        assertSame(
            Arrangement.Start,
            HistoryEntryLayout.horizontalArrangement(isMe = false)
        )
    }

    @Test
    fun `keeps my avatar gap tighter than partner gap`() {
        assertEquals(4.dp, HistoryEntryLayout.avatarGap(isMe = true))
        assertEquals(12.dp, HistoryEntryLayout.avatarGap(isMe = false))
    }

    @Test
    fun `keeps my and partner message bubbles the same width`() {
        assertEquals(
            HistoryBubbleWidth.Fixed(260.dp),
            HistoryEntryLayout.bubbleWidth(isMe = true)
        )
        assertEquals(
            HistoryBubbleWidth.Fixed(260.dp),
            HistoryEntryLayout.bubbleWidth(isMe = false)
        )
    }

    @Test
    fun `aligns my bubble content to the end`() {
        assertSame(Alignment.End, HistoryEntryLayout.bubbleContentAlignment(isMe = true))
        assertSame(Alignment.Start, HistoryEntryLayout.bubbleContentAlignment(isMe = false))
    }

    @Test
    fun `aligns my card row content to the right`() {
        assertSame(Arrangement.End, HistoryEntryLayout.cardContentArrangement(isMe = true))
        assertSame(Arrangement.Start, HistoryEntryLayout.cardContentArrangement(isMe = false))
    }

    @Test
    fun `sets horizontal card rhythm for avatar emoji and text layout`() {
        assertEquals(72.dp, HistoryEntryLayout.cardMinHeight())
        assertEquals(12.dp, HistoryEntryLayout.cardHorizontalPadding())
        assertEquals(44.dp, HistoryEntryLayout.moodBadgeSize())
        assertEquals(14.dp, HistoryEntryLayout.moodToTextGap())
    }
}
