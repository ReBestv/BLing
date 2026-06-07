package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Test

class FeelingTest {

    @Test
    fun `keeps the launch mood list in simplified Chinese`() {
        assertEquals(
            listOf(
                "开心",
                "想你",
                "亲亲",
                "爱你",
                "悠闲",
                "奋斗",
                "思考",
                "追剧",
                "难过",
                "沮丧",
                "生气",
                "焦虑",
                "疲惫",
                "生病",
                "无聊",
                "睡觉"
            ),
            Feeling.entries.map { it.displayName }
        )
    }

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
