package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Test

class FeelingTest {

    @Test
    fun cryingIsNotASelectableFeeling() {
        assertNull(Feeling.fromKey("crying"))
        assertFalse(Feeling.entries.any { it.displayName == "哭哭" })
    }

    @Test
    fun legacyCryingRowsReadAsUpset() {
        assertEquals(Feeling.UPSET, Feeling.fromLegacyLabel("哭哭"))
    }
}
