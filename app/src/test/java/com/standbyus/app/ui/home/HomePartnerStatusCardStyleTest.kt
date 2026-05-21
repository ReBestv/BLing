package com.standbyus.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomePartnerStatusCardStyleTest {

    @Test
    fun `makes partner status card emoji and primary text prominent`() {
        assertEquals(156f, HomePartnerStatusCardStyle.emojiSizeDp, 0.001f)
        assertEquals(104f, HomePartnerStatusCardStyle.emojiTextSizeSp, 0.001f)
        assertEquals(36f, HomePartnerStatusCardStyle.moodTextSizeSp, 0.001f)
        assertEquals(20f, HomePartnerStatusCardStyle.doingTextSizeSp, 0.001f)
    }

    @Test
    fun `keeps partner status card compact enough to show mood text`() {
        assertEquals(32f, HomePartnerStatusCardStyle.verticalPaddingDp, 0.001f)
        assertEquals(2f, HomePartnerStatusCardStyle.contentGapDp, 0.001f)
        assertEquals(-8f, HomePartnerStatusCardStyle.emojiLiftDp, 0.001f)
    }
}
