package com.standbyus.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePartnerStatusCardStyleTest {

    @Test
    fun `makes partner status card emoji and primary text prominent`() {
        assertEquals(188f, HomePartnerStatusCardStyle.emojiSizeDp, 0.001f)
        assertEquals(124f, HomePartnerStatusCardStyle.emojiTextSizeSp, 0.001f)
        assertEquals(36f, HomePartnerStatusCardStyle.moodTextSizeSp, 0.001f)
        assertEquals(20f, HomePartnerStatusCardStyle.doingTextSizeSp, 0.001f)
    }
}
