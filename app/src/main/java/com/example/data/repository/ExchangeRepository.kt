package com.example.data.repository

import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.data.remote.CryptoApiService
import com.example.data.remote.TgjuApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class ExchangeRepository(
    private val watchlistDao: WatchlistDao,
    private val tgjuService: TgjuApiService = TgjuApiService(),
    private val cryptoService: CryptoApiService = CryptoApiService()
) {
    private val _itemsFlow = MutableStateFlow<List<ExchangeItem>>(createInitialItems())
    val rawItemsFlow = _itemsFlow.asStateFlow()

    // Combined items flow with Watchlist favorites from Room
    val allItems: Flow<List<ExchangeItem>> = combine(_itemsFlow, watchlistDao.getAllWatchlist()) { items, watchlist ->
        val watchlistMap = watchlist.associateBy { it.id }
        items.map { item ->
            val entity = watchlistMap[item.id]
            item.copy(
                isFavorite = entity?.isFavorite == true,
                alertPriceToman = entity?.alertTargetPriceToman
            )
        }
    }

    suspend fun refreshRates(): Result<Unit> {
        return try {
            val tgjuQuotes = tgjuService.fetchTgjuRates()
            val currentItems = _itemsFlow.value.toMutableList()
            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            var usedTgju = false

            if (tgjuQuotes.isNotEmpty()) {
                usedTgju = true
                val dollarQuote = tgjuQuotes["price_dollar_rl"]
                val usdDollarToman = if (dollarQuote != null && dollarQuote.priceRials > 0) {
                    dollarQuote.priceRials / 10L
                } else {
                    currentItems.find { it.id == "USD" }?.priceToman ?: 258465L
                }

                // Map TGJU keys to items
                for (i in currentItems.indices) {
                    val item = currentItems[i]
                    val tgjuKey = when (item.id) {
                        "USD" -> "price_dollar_rl"
                        "EUR" -> "price_eur"
                        "AED" -> "price_aed"
                        "GBP" -> "price_gbp"
                        "CAD" -> "price_cad"
                        "TRY" -> "price_try"
                        "GOLD_18K" -> "geram18"
                        "GOLD_24K" -> "geram24"
                        "SEKKE_EMAMI" -> "sekee"
                        "SEKKE_BAHAR" -> "sekeb"
                        "SEKKE_NIM" -> "nim"
                        "SEKKE_ROB" -> "rob"
                        "SEKKE_GERMI" -> "gerami"
                        "OUNCE_GOLD" -> "ons"
                        "BTC" -> "crypto-bitcoin"
                        "ETH" -> "crypto-ethereum"
                        "SOL" -> "crypto-solana"
                        "BNB" -> "crypto-binance-coin"
                        "XRP" -> "crypto-ripple"
                        "DOGE" -> "crypto-dogecoin"
                        "ADA" -> "crypto-cardano"
                        "TRX" -> "crypto-tron"
                        "SHIB" -> "crypto-shiba-inu"
                        "TON" -> "crypto-toncoin"
                        else -> null
                    }

                    if (tgjuKey != null && tgjuQuotes.containsKey(tgjuKey)) {
                        val quote = tgjuQuotes[tgjuKey]!!
                        val updatedHistory = item.historyPoints.toMutableList()

                        if (item.type == AssetType.CRYPTO) {
                            val priceUsd = quote.priceUsd
                            val irrKey = "$tgjuKey-irr"
                            val irrQuote = tgjuQuotes[irrKey]
                            val priceToman = if (irrQuote != null && irrQuote.priceRials > 0) {
                                irrQuote.priceRials / 10L
                            } else {
                                (priceUsd * usdDollarToman).toLong()
                            }

                            if (updatedHistory.size >= 7) updatedHistory.removeAt(0)
                            updatedHistory.add(priceUsd)

                            val highUsd = if (quote.highUsd > 0) quote.highUsd else priceUsd * 1.02
                            val lowUsd = if (quote.lowUsd > 0) quote.lowUsd else priceUsd * 0.98

                            currentItems[i] = item.copy(
                                priceUsd = priceUsd,
                                priceToman = priceToman,
                                changePercent24h = quote.changePercent,
                                high24hUsd = highUsd,
                                low24hUsd = lowUsd,
                                high24hToman = (highUsd * usdDollarToman).toLong(),
                                low24hToman = (lowUsd * usdDollarToman).toLong(),
                                historyPoints = updatedHistory,
                                lastUpdated = if (quote.timeFa.isNotBlank()) "TGJU ${quote.timeFa}" else "TGJU $timeString"
                            )
                        } else if (item.id == "OUNCE_GOLD") {
                            val priceUsd = quote.priceUsd
                            val priceToman = (priceUsd * usdDollarToman).toLong()
                            if (updatedHistory.size >= 7) updatedHistory.removeAt(0)
                            updatedHistory.add(priceUsd)

                            currentItems[i] = item.copy(
                                priceUsd = priceUsd,
                                priceToman = priceToman,
                                changePercent24h = quote.changePercent,
                                high24hUsd = quote.highUsd,
                                low24hUsd = quote.lowUsd,
                                high24hToman = (quote.highUsd * usdDollarToman).toLong(),
                                low24hToman = (quote.lowUsd * usdDollarToman).toLong(),
                                historyPoints = updatedHistory,
                                lastUpdated = if (quote.timeFa.isNotBlank()) "TGJU ${quote.timeFa}" else "TGJU $timeString"
                            )
                        } else {
                            // Fiat & Gold in Rials -> divide by 10 to get Tomans
                            val priceToman = quote.priceRials / 10L
                            val highToman = quote.highRials / 10L
                            val lowToman = quote.lowRials / 10L
                            val priceUsd = if (usdDollarToman > 0) priceToman.toDouble() / usdDollarToman.toDouble() else item.priceUsd

                            if (updatedHistory.size >= 7) updatedHistory.removeAt(0)
                            updatedHistory.add(priceToman.toDouble())

                            currentItems[i] = item.copy(
                                priceToman = priceToman,
                                priceUsd = priceUsd,
                                changePercent24h = quote.changePercent,
                                high24hToman = highToman,
                                low24hToman = lowToman,
                                historyPoints = updatedHistory,
                                lastUpdated = if (quote.timeFa.isNotBlank()) "TGJU ${quote.timeFa}" else "TGJU $timeString"
                            )
                        }
                    } else if (item.id == "USDT") {
                        // Tether is closely aligned with Dollar
                        val usdtToman = (usdDollarToman * 1.002).toLong()
                        currentItems[i] = item.copy(
                            priceToman = usdtToman,
                            priceUsd = 1.001,
                            lastUpdated = "TGJU $timeString"
                        )
                    }
                }
            }

            // If TGJU didn't return cryptos or was partial, enrich with CryptoApiService
            if (!usedTgju) {
                val liveQuotes = cryptoService.fetchLiveCryptoRates()
                val usdItem = currentItems.find { it.id == "USD" }
                val usdRate = usdItem?.priceToman ?: 94800L

                if (liveQuotes.isNotEmpty()) {
                    val quoteMap = liveQuotes.associateBy { it.symbol }
                    for (i in currentItems.indices) {
                        val item = currentItems[i]
                        if (item.type == AssetType.CRYPTO) {
                            val quote = quoteMap[item.symbol]
                            if (quote != null) {
                                val newTomanPrice = (quote.priceUsd * usdRate).toLong()
                                val updatedHistory = item.historyPoints.toMutableList()
                                if (updatedHistory.size >= 7) updatedHistory.removeAt(0)
                                updatedHistory.add(quote.priceUsd)

                                currentItems[i] = item.copy(
                                    priceUsd = quote.priceUsd,
                                    priceToman = newTomanPrice,
                                    changePercent24h = quote.changePercent24h,
                                    high24hUsd = quote.high24h,
                                    low24hUsd = quote.low24h,
                                    high24hToman = (quote.high24h * usdRate).toLong(),
                                    low24hToman = (quote.low24h * usdRate).toLong(),
                                    historyPoints = updatedHistory,
                                    lastUpdated = "$timeString بروز"
                                )
                            }
                        }
                    }
                }
            }

            _itemsFlow.value = currentItems
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(itemId: String, currentFavorite: Boolean) {
        if (currentFavorite) {
            val existing = watchlistDao.getById(itemId)
            if (existing?.alertTargetPriceToman == null) {
                watchlistDao.deleteById(itemId)
            } else {
                watchlistDao.insertOrUpdate(existing.copy(isFavorite = false))
            }
        } else {
            val existing = watchlistDao.getById(itemId)
            if (existing != null) {
                watchlistDao.insertOrUpdate(existing.copy(isFavorite = true))
            } else {
                watchlistDao.insertOrUpdate(WatchlistEntity(id = itemId, isFavorite = true))
            }
        }
    }

    suspend fun setTargetAlert(itemId: String, targetToman: Long?) {
        val existing = watchlistDao.getById(itemId)
        if (existing != null) {
            watchlistDao.insertOrUpdate(existing.copy(alertTargetPriceToman = targetToman))
        } else {
            watchlistDao.insertOrUpdate(
                WatchlistEntity(
                    id = itemId,
                    isFavorite = false,
                    alertTargetPriceToman = targetToman
                )
            )
        }
    }

    private fun createInitialItems(): List<ExchangeItem> {
        return listOf(
            // DOLLAR & FIAT (TGJU initial alignment)
            ExchangeItem(
                id = "USD",
                symbol = "USD",
                nameFa = "دلار آمریکا (آزاد)",
                nameEn = "US Dollar (TGJU)",
                priceToman = 258465,
                priceUsd = 1.0,
                changePercent24h = 1.45,
                high24hToman = 258520,
                low24hToman = 254660,
                high24hUsd = 1.0,
                low24hUsd = 1.0,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(254660.0, 255500.0, 256200.0, 257100.0, 257900.0, 258200.0, 258465.0)
            ),
            ExchangeItem(
                id = "USDT",
                symbol = "USDT",
                nameFa = "تتر (دلار دیجیتال)",
                nameEn = "Tether USD",
                priceToman = 259000,
                priceUsd = 1.001,
                changePercent24h = 1.25,
                high24hToman = 259800,
                low24hToman = 255100,
                high24hUsd = 1.002,
                low24hUsd = 0.999,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(255100.0, 256000.0, 257200.0, 257800.0, 258400.0, 258900.0, 259000.0)
            ),
            ExchangeItem(
                id = "EUR",
                symbol = "EUR",
                nameFa = "یورو اروپا",
                nameEn = "Euro (TGJU)",
                priceToman = 292350,
                priceUsd = 1.088,
                changePercent24h = 0.85,
                high24hToman = 292640,
                low24hToman = 288530,
                high24hUsd = 1.092,
                low24hUsd = 1.084,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(288530.0, 289100.0, 290200.0, 291100.0, 291800.0, 292100.0, 292350.0)
            ),
            ExchangeItem(
                id = "AED",
                symbol = "AED",
                nameFa = "درهم امارات",
                nameEn = "UAE Dirham (TGJU)",
                priceToman = 70800,
                priceUsd = 0.272,
                changePercent24h = 1.10,
                high24hToman = 70807,
                low24hToman = 69761,
                high24hUsd = 0.273,
                low24hUsd = 0.271,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(69761.0, 69950.0, 70200.0, 70400.0, 70600.0, 70750.0, 70800.0)
            ),
            ExchangeItem(
                id = "GBP",
                symbol = "GBP",
                nameFa = "پوند انگلیس",
                nameEn = "British Pound (TGJU)",
                priceToman = 342420,
                priceUsd = 1.301,
                changePercent24h = 0.55,
                high24hToman = 342810,
                low24hToman = 337650,
                high24hUsd = 1.305,
                low24hUsd = 1.297,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(337650.0, 338900.0, 340100.0, 341200.0, 341800.0, 342100.0, 342420.0)
            ),
            ExchangeItem(
                id = "CAD",
                symbol = "CAD",
                nameFa = "دلار کانادا",
                nameEn = "Canadian Dollar (TGJU)",
                priceToman = 181840,
                priceUsd = 0.721,
                changePercent24h = 0.60,
                high24hToman = 181880,
                low24hToman = 179190,
                high24hUsd = 0.724,
                low24hUsd = 0.718,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(179190.0, 179800.0, 180400.0, 181000.0, 181400.0, 181700.0, 181840.0)
            ),
            ExchangeItem(
                id = "TRY",
                symbol = "TRY",
                nameFa = "لیر ترکیه",
                nameEn = "Turkish Lira (TGJU)",
                priceToman = 5330,
                priceUsd = 0.029,
                changePercent24h = -0.20,
                high24hToman = 5330,
                low24hToman = 5260,
                high24hUsd = 0.030,
                low24hUsd = 0.028,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(5260.0, 5280.0, 5300.0, 5310.0, 5320.0, 5325.0, 5330.0)
            ),
            ExchangeItem(
                id = "CHF",
                symbol = "CHF",
                nameFa = "فرانک سوئیس",
                nameEn = "Swiss Franc (TGJU)",
                priceToman = 108450,
                priceUsd = 1.148,
                changePercent24h = 0.40,
                high24hToman = 108900,
                low24hToman = 107800,
                high24hUsd = 1.152,
                low24hUsd = 1.142,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(107800.0, 108000.0, 108200.0, 108350.0, 108400.0, 108420.0, 108450.0)
            ),
            ExchangeItem(
                id = "CNY",
                symbol = "CNY",
                nameFa = "یوان چین",
                nameEn = "Chinese Yuan (TGJU)",
                priceToman = 13180,
                priceUsd = 0.139,
                changePercent24h = 0.25,
                high24hToman = 13240,
                low24hToman = 13100,
                high24hUsd = 0.141,
                low24hUsd = 0.137,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(13100.0, 13120.0, 13140.0, 13160.0, 13170.0, 13175.0, 13180.0)
            ),
            ExchangeItem(
                id = "AUD",
                symbol = "AUD",
                nameFa = "دلار استرالیا",
                nameEn = "Australian Dollar (TGJU)",
                priceToman = 61850,
                priceUsd = 0.654,
                changePercent24h = 0.35,
                high24hToman = 62100,
                low24hToman = 61400,
                high24hUsd = 0.658,
                low24hUsd = 0.650,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(61400.0, 61500.0, 61650.0, 61750.0, 61800.0, 61820.0, 61850.0)
            ),
            ExchangeItem(
                id = "SAR",
                symbol = "SAR",
                nameFa = "ریال عربستان",
                nameEn = "Saudi Riyal (TGJU)",
                priceToman = 25210,
                priceUsd = 0.266,
                changePercent24h = 0.18,
                high24hToman = 25300,
                low24hToman = 25100,
                high24hUsd = 0.268,
                low24hUsd = 0.265,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(25100.0, 25140.0, 25170.0, 25190.0, 25200.0, 25205.0, 25210.0)
            ),
            ExchangeItem(
                id = "GEL",
                symbol = "GEL",
                nameFa = "لاری گرجستان",
                nameEn = "Georgian Lari",
                priceToman = 34850,
                priceUsd = 0.368,
                changePercent24h = 0.50,
                high24hToman = 35100,
                low24hToman = 34600,
                high24hUsd = 0.372,
                low24hUsd = 0.365,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(34600.0, 34650.0, 34700.0, 34780.0, 34820.0, 34840.0, 34850.0)
            ),

            // CRYPTOCURRENCIES (TGJU Quotes)
            ExchangeItem(
                id = "BTC",
                symbol = "BTC",
                nameFa = "بیت‌کوین",
                nameEn = "Bitcoin (TGJU)",
                priceToman = 22275870200L,
                priceUsd = 85374.33,
                changePercent24h = 0.64,
                high24hToman = 22500000000L,
                low24hToman = 21900000000L,
                high24hUsd = 86200.0,
                low24hUsd = 84800.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(84800.0, 85000.0, 85200.0, 85100.0, 85300.0, 85350.0, 85374.0)
            ),
            ExchangeItem(
                id = "ETH",
                symbol = "ETH",
                nameFa = "اتریوم",
                nameEn = "Ethereum (TGJU)",
                priceToman = 705037150L,
                priceUsd = 2702.12,
                changePercent24h = -0.04,
                high24hToman = 715000000L,
                low24hToman = 698000000L,
                high24hUsd = 2740.0,
                low24hUsd = 2680.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(2730.0, 2720.0, 2715.0, 2708.0, 2705.0, 2703.0, 2702.0)
            ),
            ExchangeItem(
                id = "SOL",
                symbol = "SOL",
                nameFa = "سولانا",
                nameEn = "Solana (TGJU)",
                priceToman = 31250000L,
                priceUsd = 119.92,
                changePercent24h = 1.30,
                high24hToman = 32000000L,
                low24hToman = 30800000L,
                high24hUsd = 122.5,
                low24hUsd = 117.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(117.0, 117.8, 118.5, 119.0, 119.4, 119.7, 119.92)
            ),
            ExchangeItem(
                id = "BNB",
                symbol = "BNB",
                nameFa = "بایننس کوین",
                nameEn = "BNB (TGJU)",
                priceToman = 202079930L,
                priceUsd = 774.49,
                changePercent24h = 0.57,
                high24hToman = 205000000L,
                low24hToman = 199000000L,
                high24hUsd = 782.0,
                low24hUsd = 765.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(765.0, 768.0, 770.0, 772.0, 773.0, 774.0, 774.49)
            ),
            ExchangeItem(
                id = "XRP",
                symbol = "XRP",
                nameFa = "ریپل",
                nameEn = "XRP (TGJU)",
                priceToman = 485000L,
                priceUsd = 1.86,
                changePercent24h = 2.45,
                high24hToman = 500000L,
                low24hToman = 470000L,
                high24hUsd = 1.92,
                low24hUsd = 1.78,
                type = AssetType.CRYPTO,
                historyPoints = listOf(1.78, 1.80, 1.82, 1.84, 1.85, 1.855, 1.86)
            ),
            ExchangeItem(
                id = "TON",
                symbol = "TON",
                nameFa = "تون‌کوین",
                nameEn = "Toncoin (TGJU)",
                priceToman = 428000L,
                priceUsd = 1.64,
                changePercent24h = 0.15,
                high24hToman = 445000L,
                low24hToman = 420000L,
                high24hUsd = 1.72,
                low24hUsd = 1.60,
                type = AssetType.CRYPTO,
                historyPoints = listOf(1.61, 1.62, 1.63, 1.635, 1.64, 1.64, 1.64)
            ),
            ExchangeItem(
                id = "DOGE",
                symbol = "DOGE",
                nameFa = "دوج‌کوین",
                nameEn = "Dogecoin (TGJU)",
                priceToman = 24830L,
                priceUsd = 0.0951,
                changePercent24h = 0.30,
                high24hToman = 25500L,
                low24hToman = 24200L,
                high24hUsd = 0.098,
                low24hUsd = 0.092,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.092, 0.093, 0.094, 0.0945, 0.095, 0.095, 0.0951)
            ),
            ExchangeItem(
                id = "ADA",
                symbol = "ADA",
                nameFa = "کاردانو",
                nameEn = "Cardano (TGJU)",
                priceToman = 65690L,
                priceUsd = 0.2517,
                changePercent24h = 1.36,
                high24hToman = 67500L,
                low24hToman = 64200L,
                high24hUsd = 0.260,
                low24hUsd = 0.245,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.245, 0.247, 0.249, 0.250, 0.251, 0.2515, 0.2517)
            ),
            ExchangeItem(
                id = "TRX",
                symbol = "TRX",
                nameFa = "ترون",
                nameEn = "TRON (TGJU)",
                priceToman = 87400L,
                priceUsd = 0.335,
                changePercent24h = -0.12,
                high24hToman = 89000L,
                low24hToman = 86500L,
                high24hUsd = 0.342,
                low24hUsd = 0.331,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.338, 0.337, 0.336, 0.336, 0.335, 0.335, 0.335)
            ),
            ExchangeItem(
                id = "SHIB",
                symbol = "SHIB",
                nameFa = "شیبا اینو",
                nameEn = "Shiba Inu (TGJU)",
                priceToman = 2L,
                priceUsd = 0.00000586,
                changePercent24h = 1.03,
                high24hToman = 2L,
                low24hToman = 1L,
                high24hUsd = 0.0000061,
                low24hUsd = 0.0000056,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.0000056, 0.0000057, 0.0000058, 0.00000582, 0.00000585, 0.00000586, 0.00000586)
            ),
            ExchangeItem(
                id = "AVAX",
                symbol = "AVAX",
                nameFa = "آوالانچ",
                nameEn = "Avalanche",
                priceToman = 2650000L,
                priceUsd = 27.85,
                changePercent24h = 2.10,
                high24hToman = 2720000L,
                low24hToman = 2580000L,
                high24hUsd = 28.5,
                low24hUsd = 26.9,
                type = AssetType.CRYPTO,
                historyPoints = listOf(26.9, 27.1, 27.3, 27.5, 27.7, 27.8, 27.85)
            ),
            ExchangeItem(
                id = "LINK",
                symbol = "LINK",
                nameFa = "چین‌لینک",
                nameEn = "Chainlink",
                priceToman = 1580000L,
                priceUsd = 16.55,
                changePercent24h = 1.45,
                high24hToman = 1620000L,
                low24hToman = 1540000L,
                high24hUsd = 16.9,
                low24hUsd = 16.1,
                type = AssetType.CRYPTO,
                historyPoints = listOf(16.1, 16.2, 16.3, 16.4, 16.5, 16.52, 16.55)
            ),
            ExchangeItem(
                id = "DOT",
                symbol = "DOT",
                nameFa = "پولکادات",
                nameEn = "Polkadot",
                priceToman = 765000L,
                priceUsd = 8.05,
                changePercent24h = 0.80,
                high24hToman = 780000L,
                low24hToman = 750000L,
                high24hUsd = 8.25,
                low24hUsd = 7.90,
                type = AssetType.CRYPTO,
                historyPoints = listOf(7.9, 7.95, 8.0, 8.02, 8.03, 8.04, 8.05)
            ),
            ExchangeItem(
                id = "PEPE",
                symbol = "PEPE",
                nameFa = "پپه",
                nameEn = "Pepe",
                priceToman = 1150L,
                priceUsd = 0.0000121,
                changePercent24h = 4.20,
                high24hToman = 1220L,
                low24hToman = 1080L,
                high24hUsd = 0.0000128,
                low24hUsd = 0.0000114,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.0000114, 0.0000116, 0.0000118, 0.0000119, 0.0000120, 0.0000121, 0.0000121)
            ),
            ExchangeItem(
                id = "NOT",
                symbol = "NOT",
                nameFa = "نات‌کوین",
                nameEn = "Notcoin",
                priceToman = 745L,
                priceUsd = 0.0078,
                changePercent24h = 1.85,
                high24hToman = 780L,
                low24hToman = 720L,
                high24hUsd = 0.0082,
                low24hUsd = 0.0075,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.0075, 0.0076, 0.00765, 0.0077, 0.00775, 0.0078, 0.0078)
            ),

            // GOLD & COINS (TGJU Official Rates)
            ExchangeItem(
                id = "GOLD_18K",
                symbol = "GOLD18",
                nameFa = "طلای ۱۸ عیار (هر گرم)",
                nameEn = "Gold 18K (TGJU)",
                priceToman = 25694400L,
                priceUsd = 99.4,
                changePercent24h = 0.15,
                high24hToman = 25787200L,
                low24hToman = 25300100L,
                high24hUsd = 100.5,
                low24hUsd = 97.8,
                type = AssetType.GOLD,
                historyPoints = listOf(25300100.0, 25420000.0, 25510000.0, 25600000.0, 25650000.0, 25680000.0, 25694400.0),
                unit = "تومان/گرم"
            ),
            ExchangeItem(
                id = "GOLD_24K",
                symbol = "GOLD24",
                nameFa = "طلای ۲۴ عیار (هر گرم)",
                nameEn = "Gold 24K (TGJU)",
                priceToman = 34258800L,
                priceUsd = 132.5,
                changePercent24h = 0.15,
                high24hToman = 34382600L,
                low24hToman = 33733100L,
                high24hUsd = 133.2,
                low24hUsd = 130.5,
                type = AssetType.GOLD,
                historyPoints = listOf(33733100.0, 33850000.0, 34010000.0, 34120000.0, 34200000.0, 34240000.0, 34258800.0),
                unit = "تومان/گرم"
            ),
            ExchangeItem(
                id = "SEKKE_EMAMI",
                symbol = "EMAMI",
                nameFa = "سکه امامی (تمام بهار)",
                nameEn = "Emami Coin (TGJU)",
                priceToman = 260395000L,
                priceUsd = 1007.4,
                changePercent24h = 0.25,
                high24hToman = 260810000L,
                low24hToman = 258980000L,
                high24hUsd = 1015.0,
                low24hUsd = 998.0,
                type = AssetType.GOLD,
                historyPoints = listOf(258980000.0, 259200000.0, 259600000.0, 260100000.0, 260250000.0, 260350000.0, 260395000.0),
                unit = "تومان"
            ),
            ExchangeItem(
                id = "SEKKE_BAHAR",
                symbol = "BAHAR",
                nameFa = "سکه بهار آزادی (طرح قدیم)",
                nameEn = "Bahar Azadi (TGJU)",
                priceToman = 252065000L,
                priceUsd = 975.2,
                changePercent24h = 0.20,
                high24hToman = 252450000L,
                low24hToman = 251450000L,
                high24hUsd = 980.0,
                low24hUsd = 970.0,
                type = AssetType.GOLD,
                historyPoints = listOf(251450000.0, 251600000.0, 251800000.0, 251950000.0, 252000000.0, 252040000.0, 252065000.0),
                unit = "تومان"
            ),
            ExchangeItem(
                id = "SEKKE_NIM",
                symbol = "NIM",
                nameFa = "نیم سکه بهار آزادی",
                nameEn = "Half Coin (TGJU)",
                priceToman = 135740000L,
                priceUsd = 525.1,
                changePercent24h = 0.18,
                high24hToman = 135900000L,
                low24hToman = 135000000L,
                high24hUsd = 530.0,
                low24hUsd = 521.0,
                type = AssetType.GOLD,
                historyPoints = listOf(135000000.0, 135200000.0, 135400000.0, 135550000.0, 135650000.0, 135700000.0, 135740000.0),
                unit = "تومان"
            ),
            ExchangeItem(
                id = "SEKKE_ROB",
                symbol = "ROB",
                nameFa = "ربع سکه بهار آزادی",
                nameEn = "Quarter Coin (TGJU)",
                priceToman = 73100000L,
                priceUsd = 282.8,
                changePercent24h = 0.35,
                high24hToman = 73480000L,
                low24hToman = 72900000L,
                high24hUsd = 286.0,
                low24hUsd = 280.0,
                type = AssetType.GOLD,
                historyPoints = listOf(72900000.0, 72950000.0, 73000000.0, 73050000.0, 73080000.0, 73090000.0, 73100000.0),
                unit = "تومان"
            ),
            ExchangeItem(
                id = "SEKKE_GERMI",
                symbol = "GERMI",
                nameFa = "سکه گرمی بانک مرکزی",
                nameEn = "Gram Coin (TGJU)",
                priceToman = 37000000L,
                priceUsd = 143.1,
                changePercent24h = 0.10,
                high24hToman = 37000000L,
                low24hToman = 37000000L,
                high24hUsd = 143.5,
                low24hUsd = 142.8,
                type = AssetType.GOLD,
                historyPoints = listOf(37000000.0, 37000000.0, 37000000.0, 37000000.0, 37000000.0, 37000000.0, 37000000.0),
                unit = "تومان"
            ),
            ExchangeItem(
                id = "OUNCE_GOLD",
                symbol = "XAU",
                nameFa = "انس جهانی طلا",
                nameEn = "Gold Ounce (TGJU)",
                priceToman = 1069430000L,
                priceUsd = 4137.58,
                changePercent24h = -0.91,
                high24hToman = 1091500000L,
                low24hToman = 1068900000L,
                high24hUsd = 4222.95,
                low24hUsd = 4135.82,
                type = AssetType.GOLD,
                historyPoints = listOf(4210.0, 4195.0, 4180.0, 4165.0, 4150.0, 4142.0, 4137.58),
                unit = "دلار/انس"
            )
        )
    }
}
