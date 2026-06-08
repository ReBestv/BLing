package com.standbyus.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DoingTest {

    @Test
    fun `keeps the launch activity list in simplified Chinese`() {
        assertEquals(
            listOf(
                "搬砖",
                "加班",
                "学习",
                "睡觉",
                "干饭",
                "运动",
                "通勤",
                "看电影",
                "玩游戏",
                "发呆",
                "桌游",
                "自定义"
            ),
            Doing.entries.map { it.displayName }
        )
    }

    @Test
    fun `finds activity by display name`() {
        assertEquals(Doing.BOARD_GAME, Doing.fromDisplayName("桌游"))
        assertEquals(Doing.CUSTOM, Doing.fromDisplayName("自定义"))
    }
}
