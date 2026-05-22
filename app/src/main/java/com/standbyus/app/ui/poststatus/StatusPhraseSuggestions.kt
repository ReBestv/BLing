package com.standbyus.app.ui.poststatus

import com.standbyus.app.data.model.Feeling

object StatusPhraseSuggestions {
    private val fallback = listOf("在忙，晚点找你", "有点累", "想被哄一下")

    private val phrasesByFeelingKey = mapOf(
        Feeling.HAPPY.key to listOf("今天还不错", "想和你分享", "等下讲给你听"),
        Feeling.MISSING.key to listOf("想你啦", "想被抱抱", "什么时候见面"),
        Feeling.KISS.key to listOf("亲亲一下", "想贴贴", "给你一个亲亲"),
        Feeling.LOVE.key to listOf("爱你呀", "今天也喜欢你", "想抱你"),
        Feeling.RELAXED.key to listOf("慢悠悠的一天", "正在放空", "想一起散会儿步"),
        Feeling.HUSTLING.key to listOf("在忙", "晚点找你", "今天也在努力"),
        Feeling.THINKING.key to listOf("在想事情", "等我理一下", "想听听你的想法"),
        Feeling.WATCHING.key to listOf("在追剧", "晚点聊剧情", "想一起看"),
        Feeling.SAD.key to listOf("有点难过", "想被安慰", "陪我一下"),
        Feeling.UPSET.key to listOf("有点委屈", "想被哄一下", "先让我缓缓"),
        Feeling.ANGRY.key to listOf("有点生气", "让我冷静一下", "想你哄哄我"),
        Feeling.ANXIOUS.key to listOf("有点慌", "陪我一下", "想听你说句话"),
        Feeling.TIRED.key to listOf("有点累", "想休息一下", "需要充电"),
        Feeling.SICK.key to listOf("有点不舒服", "想被照顾", "今天慢一点"),
        Feeling.BORED.key to listOf("有点无聊", "找我玩嘛", "想听你说话"),
        Feeling.SLEEPING.key to listOf("准备睡啦", "晚安", "明天见")
    )

    fun forFeeling(feeling: Feeling): List<String> = forFeelingKey(feeling.key)

    fun forFeelingKey(key: String): List<String> = phrasesByFeelingKey[key] ?: fallback

    fun fallbackPhrases(): List<String> = fallback
}
