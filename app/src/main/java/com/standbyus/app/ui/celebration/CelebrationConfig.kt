package com.standbyus.app.ui.celebration

object CelebrationConfig {
    val days = listOf(
        CelebrationDay(
            id = "new_year",
            month = 1,
            day = 1,
            emoji = "🎆",
            message = "元旦快乐！",
            style = CelebrationStyle.FIREWORKS,
            priority = 20
        ),
        CelebrationDay(
            id = "valentine",
            month = 2,
            day = 14,
            emoji = "💕",
            message = "情人节快乐！",
            style = CelebrationStyle.HEARTS,
            priority = 40
        ),
        CelebrationDay(
            id = "birthday_0309",
            month = 3,
            day = 9,
            emoji = "🎂",
            message = "生日快乐！",
            style = CelebrationStyle.BIRTHDAY,
            priority = 100
        ),
        CelebrationDay(
            id = "love_520",
            month = 5,
            day = 20,
            emoji = "💗",
            message = "520快乐！",
            style = CelebrationStyle.HEARTS,
            priority = 40
        ),
        CelebrationDay(
            id = "birthday_0624",
            month = 6,
            day = 24,
            emoji = "🎂",
            message = "生日快乐！",
            style = CelebrationStyle.BIRTHDAY,
            priority = 100
        ),
        CelebrationDay(
            id = "national_day",
            month = 10,
            day = 1,
            emoji = "⭐",
            message = "国庆快乐！",
            style = CelebrationStyle.RED_GOLD,
            priority = 20
        ),
        CelebrationDay(
            id = "christmas",
            month = 12,
            day = 25,
            emoji = "🎄",
            message = "圣诞快乐！",
            style = CelebrationStyle.CHRISTMAS,
            priority = 30
        ),
    )

    val previewDays = listOf(
        CelebrationDay(
            id = "preview_birthday",
            month = 0,
            day = 0,
            emoji = "🎂",
            message = "生日快乐！",
            style = CelebrationStyle.BIRTHDAY,
            priority = 0
        ),
        CelebrationDay(
            id = "preview_hearts",
            month = 0,
            day = 0,
            emoji = "💗",
            message = "520快乐！",
            style = CelebrationStyle.HEARTS,
            priority = 0
        ),
        CelebrationDay(
            id = "preview_fireworks",
            month = 0,
            day = 0,
            emoji = "🎆",
            message = "元旦快乐！",
            style = CelebrationStyle.FIREWORKS,
            priority = 0
        ),
        CelebrationDay(
            id = "preview_christmas",
            month = 0,
            day = 0,
            emoji = "🎄",
            message = "圣诞快乐！",
            style = CelebrationStyle.CHRISTMAS,
            priority = 0
        ),
        CelebrationDay(
            id = "preview_red_gold",
            month = 0,
            day = 0,
            emoji = "⭐",
            message = "国庆快乐！",
            style = CelebrationStyle.RED_GOLD,
            priority = 0
        )
    )

    fun match(
        month: Int,
        day: Int,
        source: List<CelebrationDay> = days
    ): CelebrationDay? {
        return source
            .filter { it.month == month && it.day == day }
            .maxByOrNull { it.priority }
    }

    fun displayKey(id: String, dateKey: String): String = "shown_${id}_$dateKey"
}
