package com.example.data.model

enum class AssetType(val titleFa: String, val titleEn: String) {
    ALL("همه", "All"),
    DOLLAR_FIAT("دلار و ارزها", "Currencies"),
    CRYPTO("کریپتو", "Crypto"),
    GOLD("طلا و سکه", "Gold & Coins"),
    WATCHLIST("دیده‌بان", "Watchlist")
}

enum class PriceSource(
    val nameFa: String,
    val shortName: String,
    val icon: String,
    val website: String,
    val description: String
) {
    TGJU("شبکه طلا و ارز (TGJU)", "TGJU", "🏛️", "tgju.org", "مرجع رسمی و بازار نقدی تهران"),
    BONBAST("صرافی و بازار آزاد (Bonbast)", "بن‌بست", "📈", "bonbast.com", "میانگین صرافی‌های معتبر آزاد"),
    NOBITEX("صرافی نوبیتکس (Nobitex)", "نوبیتکس", "💎", "nobitex.ir", "دفتر سفارشات زنده و حجم بالا"),
    WALLEX("صرافی والکس (Wallex)", "والکس", "🪙", "wallex.ir", "معاملات آنی و OTC کریپتو"),
    CBI_NIMA("بانک مرکزی و سامانه نیما (CBI)", "بانک مرکزی", "🏦", "cbi.ir", "نرخ حواله رسمی تجاری"),
    BINANCE("بایننس بین‌الملل (Binance)", "بایننس", "🌐", "binance.com", "بزرگترین صرافی رمزارز جهان"),
    DUBAI_FX("صرافی و حواله دبی (Dubai FX)", "حواله دبی", "🇦🇪", "dubaifx.ae", "مبنای حواله تجاری درهم و دلار")
}

data class SourceQuote(
    val source: PriceSource,
    val priceToman: Long,
    val priceUsd: Double,
    val changePercent24h: Double,
    val lastUpdate: String = "لحظاتی پیش",
    val volumeLabel: String = ""
)

enum class TimeFrame(val labelFa: String, val labelEn: String, val count: Int) {
    H1("۱ ساعت", "1H", 20),
    H24("۲۴ ساعت", "24H", 28),
    D7("۷ روز", "7D", 21),
    M1("۱ ماه", "1M", 30),
    M3("۳ ماه", "3M", 45),
    Y1("۱ سال", "1Y", 52),
    ALL("کل", "ALL", 60)
}

enum class ChartType(val labelFa: String, val icon: String) {
    AREA("خطی / موجی", "📈"),
    CANDLESTICK("کندل‌استیک OHLC", "🕯️"),
    INDICATORS("تحلیل تکنیکال MA", "📊")
}

data class HistoricalPoint(
    val timestamp: Long,
    val timeLabel: String,
    val priceToman: Long,
    val priceUsd: Double,
    val volumeNormalized: Float = 0.5f,
    val openToman: Long = priceToman,
    val highToman: Long = priceToman,
    val lowToman: Long = priceToman,
    val closeToman: Long = priceToman,
    val ma7Toman: Long? = null,
    val ma25Toman: Long? = null
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
    val lastUpdated: String = "لحظاتی پیش",
    val sources: List<SourceQuote> = emptyList(),
    val selectedSource: PriceSource = PriceSource.TGJU
)
