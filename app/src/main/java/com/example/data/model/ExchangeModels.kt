package com.example.data.model

enum class AssetType(val titleFa: String, val titleEn: String) {
    ALL("همه", "All"),
    DOLLAR_FIAT("دلار و ارزها", "Currencies"),
    CRYPTO("کریپتو", "Crypto"),
    GOLD("طلا و سکه", "Gold & Coins"),
    WATCHLIST("دیده‌بان", "Watchlist")
}

enum class TimeFrame(val labelFa: String, val labelEn: String, val count: Int) {
    H24("۲۴ ساعت", "24H", 24),
    D7("۷ روز", "7D", 14),
    M1("۱ ماه", "1M", 30),
    M3("۳ ماه", "3M", 45),
    Y1("۱ سال", "1Y", 52)
}

data class HistoricalPoint(
    val timestamp: Long,
    val timeLabel: String,
    val priceToman: Long,
    val priceUsd: Double,
    val volumeNormalized: Float
)

data class ExchangeItem(
    val id: String,
    val symbol: String,
    val nameFa: String,
    val nameEn: String,
    val priceToman: Long,
    val priceUsd: Double,
    val changePercent24h: Double,
    val high24hToman: Long,
    val low24hToman: Long,
    val high24hUsd: Double,
    val low24hUsd: Double,
    val type: AssetType,
    val historyPoints: List<Double>,
    val unit: String = "تومان",
    val isFavorite: Boolean = false,
    val alertPriceToman: Long? = null,
    val lastUpdated: String = "لحظاتی پیش"
)
