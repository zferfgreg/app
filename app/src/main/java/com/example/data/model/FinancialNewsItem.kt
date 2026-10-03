package com.example.data.model

import java.util.UUID

enum class NewsSentiment {
    BULLISH,
    BEARISH,
    NEUTRAL
}

data class FinancialNewsItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val summary: String,
    val sourceName: String,
    val sourceUrl: String? = null,
    val publishTime: String = "همین حالا",
    val sentiment: NewsSentiment = NewsSentiment.NEUTRAL,
    val relevantSymbol: String = "USD"
)
