package com.example.data.model

enum class SentimentLevel(val labelFa: String, val emoji: String) {
    EXTREME_BEARISH("خرسی شدید", "🔴"),
    BEARISH("نزولی / خرسی", "🟠"),
    NEUTRAL("خنثی و متعادل", "🟡"),
    BULLISH("صعودی / گاوی", "🟢"),
    EXTREME_BULLISH("گاوی شدید", "🚀")
}

data class MarketSentimentData(
    val score: Int = 68, // 0..100
    val level: SentimentLevel = SentimentLevel.BULLISH,
    val goldSentiment: Int = 74, // 0..100
    val currencySentiment: Int = 62, // 0..100
    val cryptoSentiment: Int = 70, // 0..100
    val volatilityIndex: Double = 2.4, // percent 24h
    val aiReasoning: String = "تقاضای انباشته در بازارهای دارایی‌های امن (طلا و سکه) همراه با حفظ سطوح حمایتی بیت‌کوین، سیگنال صعودی معتدل به بازار مخابره می‌کند.",
    val lastUpdated: String = "چند لحظه پیش"
)
