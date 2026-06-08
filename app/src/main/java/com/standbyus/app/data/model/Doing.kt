package com.standbyus.app.data.model

enum class Doing(val displayName: String) {
    WORKING("搬砖"),
    OVERTIME("加班"),
    STUDYING("学习"),
    SLEEPING("睡觉"),
    EATING("干饭"),
    EXERCISING("运动"),
    COMMUTING("通勤"),
    MOVIE("看电影"),
    GAMING("玩游戏"),
    DAYDREAMING("发呆"),
    BOARD_GAME("桌游"),
    CUSTOM("自定义");

    companion object {
        fun fromDisplayName(name: String): Doing? = entries.find { it.displayName == name }
    }
}
