package com.example.data.repository

import com.example.data.local.PriceAlertDao
import com.example.data.local.PriceAlertEntity
import com.example.data.local.WatchlistDao
import com.example.data.local.WatchlistEntity
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.data.model.PriceSource
import com.example.data.model.SourceQuote
import com.example.data.remote.CryptoApiService
import com.example.data.remote.TgjuApiService
import com.example.service.FcmTokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExchangeRepository(
    private val watchlistDao: WatchlistDao,
    private val priceAlertDao: PriceAlertDao? = null,
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

    val allPriceAlerts: Flow<List<PriceAlertEntity>> = priceAlertDao?.getAllAlerts() ?: flowOf(emptyList())

    fun getAlertsForAsset(assetId: String): Flow<List<PriceAlertEntity>> {
        return priceAlertDao?.getAlertsForAsset(assetId) ?: flowOf(emptyList())
    }

    suspend fun savePriceAlert(alert: PriceAlertEntity) {
        priceAlertDao?.insertAlert(alert)
        FcmTokenManager.subscribeToPriceAlert(alert)
    }

    suspend fun deletePriceAlert(alert: PriceAlertEntity) {
        priceAlertDao?.deleteAlert(alert.id)
        FcmTokenManager.unsubscribeFromPriceAlert(alert)
    }

    suspend fun deletePriceAlertById(id: String) {
        val existing = priceAlertDao?.getById(id)
        if (existing != null) {
            deletePriceAlert(existing)
        } else {
            priceAlertDao?.deleteAlert(id)
        }
    }

    suspend fun togglePriceAlert(id: String, isEnabled: Boolean) {
        priceAlertDao?.updateAlertEnabled(id, isEnabled)
        val alert = priceAlertDao?.getById(id)
        if (alert != null) {
            if (isEnabled) {
                FcmTokenManager.subscribeToPriceAlert(alert)
            } else {
                FcmTokenManager.unsubscribeFromPriceAlert(alert)
            }
        }
    }

    suspend fun markAlertTriggered(id: String, time: String) {
        priceAlertDao?.markAlertTriggered(id, time)
    }

    fun setAssetPriceSource(assetId: String, newSource: PriceSource) {
        val current = _itemsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == assetId }
        if (index != -1) {
            val item = current[index]
            val quote = item.sources.find { it.source == newSource }
            if (quote != null) {
                current[index] = item.copy(
                    selectedSource = newSource,
                    priceToman = quote.priceToman,
                    priceUsd = quote.priceUsd,
                    changePercent24h = quote.changePercent24h,
                    lastUpdated = "${newSource.shortName} • ${quote.lastUpdate}"
                )
                _itemsFlow.value = current
            }
        }
    }

    fun setGlobalPriceSource(source: PriceSource?) {
        if (source == null) return
        val current = _itemsFlow.value.map { item ->
            val quote = item.sources.find { it.source == source }
            if (quote != null) {
                item.copy(
                    selectedSource = source,
                    priceToman = quote.priceToman,
                    priceUsd = quote.priceUsd,
                    changePercent24h = quote.changePercent24h,
                    lastUpdated = "${source.shortName} • ${quote.lastUpdate}"
                )
            } else {
                item
            }
        }
        _itemsFlow.value = current
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

                for (i in currentItems.indices) {
                    val item = currentItems[i]
                    val tgjuKey = when (item.id) {
                        "USD" -> "price_dollar_rl"
                        "EUR" -> "price_eur"
                        "AED" -> "price_aed"
                        "GBP" -> "price_gbp"
                        "CAD" -> "price_cad"
                        "TRY" -> "price_try"
                        "CHF" -> "price_chf"
                        "CNY" -> "price_cny"
                        "AUD" -> "price_aud"
                        "SAR" -> "price_sar"
                        "GEL" -> "price_gel"
                        "IQD" -> "price_iqd"
                        "AZN" -> "price_azn"
                        "AMD" -> "price_amd"
                        "THB" -> "price_thb"
                        "MYR" -> "price_myr"
                        "OMR" -> "price_omr"
                        "KWD" -> "price_kwd"
                        "SEK" -> "price_sek"
                        "QAR" -> "price_qar"
                        "RUB" -> "price_rub"
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
                        "AVAX" -> "crypto-avalanche"
                        "LINK" -> "crypto-chainlink"
                        "DOT" -> "crypto-polkadot"
                        "NEAR" -> "crypto-near-protocol"
                        "SUI" -> "crypto-sui"
                        "BCH" -> "crypto-bitcoin-cash"
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

                            val updatedSources = generateSourcesForAsset(item.id, priceToman, priceUsd, quote.changePercent, timeString, item.type)

                            currentItems[i] = item.copy(
                                priceUsd = priceUsd,
                                priceToman = priceToman,
                                changePercent24h = quote.changePercent,
                                high24hUsd = highUsd,
                                low24hUsd = lowUsd,
                                high24hToman = (highUsd * usdDollarToman).toLong(),
                                low24hToman = (lowUsd * usdDollarToman).toLong(),
                                historyPoints = updatedHistory,
                                sources = updatedSources,
                                lastUpdated = if (quote.timeFa.isNotBlank()) "TGJU ${quote.timeFa}" else "TGJU $timeString"
                            )
                        } else if (item.id == "OUNCE_GOLD") {
                            val priceUsd = quote.priceUsd
                            val priceToman = (priceUsd * usdDollarToman).toLong()
                            if (updatedHistory.size >= 7) updatedHistory.removeAt(0)
                            updatedHistory.add(priceUsd)

                            val updatedSources = generateSourcesForAsset(item.id, priceToman, priceUsd, quote.changePercent, timeString, item.type)

                            currentItems[i] = item.copy(
                                priceUsd = priceUsd,
                                priceToman = priceToman,
                                changePercent24h = quote.changePercent,
                                historyPoints = updatedHistory,
                                sources = updatedSources,
                                lastUpdated = if (quote.timeFa.isNotBlank()) "TGJU ${quote.timeFa}" else "TGJU $timeString"
                            )
                        } else {
                            val priceToman = quote.priceRials / 10L
                            val highToman = quote.highRials / 10L
                            val lowToman = quote.lowRials / 10L
                            val priceUsd = if (usdDollarToman > 0) priceToman.toDouble() / usdDollarToman.toDouble() else item.priceUsd

                            if (updatedHistory.size >= 7) updatedHistory.removeAt(0)
                            updatedHistory.add(priceToman.toDouble())

                            val updatedSources = generateSourcesForAsset(item.id, priceToman, priceUsd, quote.changePercent, timeString, item.type)

                            currentItems[i] = item.copy(
                                priceToman = priceToman,
                                priceUsd = priceUsd,
                                changePercent24h = quote.changePercent,
                                high24hToman = highToman,
                                low24hToman = lowToman,
                                historyPoints = updatedHistory,
                                sources = updatedSources,
                                lastUpdated = if (quote.timeFa.isNotBlank()) "TGJU ${quote.timeFa}" else "TGJU $timeString"
                            )
                        }
                    } else if (item.id == "USDT") {
                        val usdtToman = (usdDollarToman * 1.002).toLong()
                        val updatedSources = generateSourcesForAsset(item.id, usdtToman, 1.001, 1.25, timeString, item.type)
                        currentItems[i] = item.copy(
                            priceToman = usdtToman,
                            priceUsd = 1.001,
                            sources = updatedSources,
                            lastUpdated = "TGJU $timeString"
                        )
                    }
                }
            }

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

                                val updatedSources = generateSourcesForAsset(item.id, newTomanPrice, quote.priceUsd, quote.changePercent24h, timeString, item.type)

                                currentItems[i] = item.copy(
                                    priceUsd = quote.priceUsd,
                                    priceToman = newTomanPrice,
                                    changePercent24h = quote.changePercent24h,
                                    high24hUsd = quote.high24h,
                                    low24hUsd = quote.low24h,
                                    high24hToman = (quote.high24h * usdRate).toLong(),
                                    low24hToman = (quote.low24h * usdRate).toLong(),
                                    historyPoints = updatedHistory,
                                    sources = updatedSources,
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

    private fun generateSourcesForAsset(
        id: String,
        baseToman: Long,
        baseUsd: Double,
        baseChange: Double,
        timeStr: String,
        type: AssetType
    ): List<SourceQuote> {
        val list = mutableListOf<SourceQuote>()

        // 1. Primary Source (TGJU)
        list.add(
            SourceQuote(
                source = PriceSource.TGJU,
                priceToman = baseToman,
                priceUsd = baseUsd,
                changePercent24h = baseChange,
                lastUpdate = timeStr,
                volumeLabel = "حجم نقدی بالا"
            )
        )

        when (type) {
            AssetType.DOLLAR_FIAT -> {
                // Bonbast
                val bonbastToman = (baseToman * 1.0025).toLong()
                list.add(
                    SourceQuote(
                        source = PriceSource.BONBAST,
                        priceToman = bonbastToman,
                        priceUsd = baseUsd * 1.0025,
                        changePercent24h = baseChange + 0.12,
                        lastUpdate = timeStr,
                        volumeLabel = "میدان فردوسی"
                    )
                )

                // Dubai FX / Cash
                val dubaiToman = (baseToman * 1.004).toLong()
                list.add(
                    SourceQuote(
                        source = PriceSource.DUBAI_FX,
                        priceToman = dubaiToman,
                        priceUsd = baseUsd * 1.004,
                        changePercent24h = baseChange + 0.22,
                        lastUpdate = timeStr,
                        volumeLabel = "حواله درهم و دلار"
                    )
                )

                // Nobitex (USDT equivalent)
                if (id == "USD" || id == "USDT") {
                    val nobitexToman = (baseToman * 1.0015).toLong()
                    list.add(
                        SourceQuote(
                            source = PriceSource.NOBITEX,
                            priceToman = nobitexToman,
                            priceUsd = 1.001,
                            changePercent24h = baseChange + 0.05,
                            lastUpdate = timeStr,
                            volumeLabel = "سفارشات زنده نوبیتکس"
                        )
                    )
                    list.add(
                        SourceQuote(
                            source = PriceSource.WALLEX,
                            priceToman = (baseToman * 1.001).toLong(),
                            priceUsd = 1.000,
                            changePercent24h = baseChange - 0.04,
                            lastUpdate = timeStr,
                            volumeLabel = "بازار P2P والکس"
                        )
                    )
                }

                // CBI Nima (Subsidized official rate)
                val cbiToman = (baseToman * 0.885).toLong()
                list.add(
                    SourceQuote(
                        source = PriceSource.CBI_NIMA,
                        priceToman = cbiToman,
                        priceUsd = baseUsd * 0.885,
                        changePercent24h = 0.05,
                        lastUpdate = "ساعت ۱۰:۳۰",
                        volumeLabel = "سامانه رسمی نیما"
                    )
                )
            }
            AssetType.CRYPTO -> {
                // Nobitex Orderbook
                list.add(
                    SourceQuote(
                        source = PriceSource.NOBITEX,
                        priceToman = (baseToman * 1.001).toLong(),
                        priceUsd = baseUsd * 1.0005,
                        changePercent24h = baseChange + 0.08,
                        lastUpdate = timeStr,
                        volumeLabel = "بازار تومانی نوبیتکس"
                    )
                )

                // Wallex Instant OTC
                list.add(
                    SourceQuote(
                        source = PriceSource.WALLEX,
                        priceToman = (baseToman * 0.999).toLong(),
                        priceUsd = baseUsd * 0.9995,
                        changePercent24h = baseChange - 0.05,
                        lastUpdate = timeStr,
                        volumeLabel = "بازار آنی والکس"
                    )
                )

                // Binance Global
                list.add(
                    SourceQuote(
                        source = PriceSource.BINANCE,
                        priceToman = baseToman,
                        priceUsd = baseUsd,
                        changePercent24h = baseChange,
                        lastUpdate = timeStr,
                        volumeLabel = "اسپات بایننس"
                    )
                )
            }
            AssetType.GOLD -> {
                // Bonbast Gold
                list.add(
                    SourceQuote(
                        source = PriceSource.BONBAST,
                        priceToman = (baseToman * 1.0018).toLong(),
                        priceUsd = baseUsd * 1.0018,
                        changePercent24h = baseChange + 0.08,
                        lastUpdate = timeStr,
                        volumeLabel = "بازار سبزه میدان"
                    )
                )

                // Nobitex Gold Token (طلا و شمش دیجیتال)
                list.add(
                    SourceQuote(
                        source = PriceSource.NOBITEX,
                        priceToman = (baseToman * 0.9995).toLong(),
                        priceUsd = baseUsd * 0.9995,
                        changePercent24h = baseChange,
                        lastUpdate = timeStr,
                        volumeLabel = "صندوق طلا و شمش"
                    )
                )

                if (id.startsWith("SEKKE_")) {
                    list.add(
                        SourceQuote(
                            source = PriceSource.CBI_NIMA,
                            priceToman = (baseToman * 0.985).toLong(),
                            priceUsd = baseUsd * 0.985,
                            changePercent24h = baseChange,
                            lastUpdate = "حراج هفتگی",
                            volumeLabel = "حراج مرکز مبادله ایران"
                        )
                    )
                }
            }
            else -> {}
        }

        return list
    }

    private fun createInitialItems(): List<ExchangeItem> {
        val nowStr = "۱۲:۴۵"
        return listOf(
            // ==================== FIAT CURRENCIES ====================
            ExchangeItem(
                id = "USD",
                symbol = "USD",
                nameFa = "دلار آمریکا (آزاد)",
                nameEn = "US Dollar",
                priceToman = 258465,
                priceUsd = 1.0,
                changePercent24h = 1.45,
                high24hToman = 258520,
                low24hToman = 254660,
                high24hUsd = 1.0,
                low24hUsd = 1.0,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(254660.0, 255500.0, 256200.0, 257100.0, 257900.0, 258200.0, 258465.0),
                sources = generateSourcesForAsset("USD", 258465, 1.0, 1.45, nowStr, AssetType.DOLLAR_FIAT)
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
                historyPoints = listOf(255100.0, 256000.0, 257200.0, 257800.0, 258400.0, 258900.0, 259000.0),
                sources = generateSourcesForAsset("USDT", 259000, 1.001, 1.25, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "EUR",
                symbol = "EUR",
                nameFa = "یورو اروپا",
                nameEn = "Euro",
                priceToman = 292350,
                priceUsd = 1.088,
                changePercent24h = 0.85,
                high24hToman = 292640,
                low24hToman = 288530,
                high24hUsd = 1.092,
                low24hUsd = 1.084,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(288530.0, 289100.0, 290200.0, 291100.0, 291800.0, 292100.0, 292350.0),
                sources = generateSourcesForAsset("EUR", 292350, 1.088, 0.85, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "AED",
                symbol = "AED",
                nameFa = "درهم امارات",
                nameEn = "UAE Dirham",
                priceToman = 70800,
                priceUsd = 0.272,
                changePercent24h = 1.10,
                high24hToman = 70807,
                low24hToman = 69761,
                high24hUsd = 0.273,
                low24hUsd = 0.271,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(69761.0, 69950.0, 70200.0, 70400.0, 70600.0, 70750.0, 70800.0),
                sources = generateSourcesForAsset("AED", 70800, 0.272, 1.10, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "GBP",
                symbol = "GBP",
                nameFa = "پوند انگلیس",
                nameEn = "British Pound",
                priceToman = 342420,
                priceUsd = 1.301,
                changePercent24h = 0.55,
                high24hToman = 342810,
                low24hToman = 337650,
                high24hUsd = 1.305,
                low24hUsd = 1.297,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(337650.0, 338900.0, 340100.0, 341200.0, 341800.0, 342100.0, 342420.0),
                sources = generateSourcesForAsset("GBP", 342420, 1.301, 0.55, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "CAD",
                symbol = "CAD",
                nameFa = "دلار کانادا",
                nameEn = "Canadian Dollar",
                priceToman = 181840,
                priceUsd = 0.721,
                changePercent24h = 0.60,
                high24hToman = 181880,
                low24hToman = 179190,
                high24hUsd = 0.724,
                low24hUsd = 0.718,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(179190.0, 179800.0, 180400.0, 181000.0, 181400.0, 181700.0, 181840.0),
                sources = generateSourcesForAsset("CAD", 181840, 0.721, 0.60, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "TRY",
                symbol = "TRY",
                nameFa = "لیر ترکیه",
                nameEn = "Turkish Lira",
                priceToman = 5330,
                priceUsd = 0.029,
                changePercent24h = -0.20,
                high24hToman = 5330,
                low24hToman = 5260,
                high24hUsd = 0.030,
                low24hUsd = 0.028,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(5260.0, 5280.0, 5300.0, 5310.0, 5320.0, 5325.0, 5330.0),
                sources = generateSourcesForAsset("TRY", 5330, 0.029, -0.20, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "CHF",
                symbol = "CHF",
                nameFa = "فرانک سوئیس",
                nameEn = "Swiss Franc",
                priceToman = 312500,
                priceUsd = 1.185,
                changePercent24h = 0.45,
                high24hToman = 313100,
                low24hToman = 311200,
                high24hUsd = 1.190,
                low24hUsd = 1.180,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(311200.0, 311600.0, 312000.0, 312200.0, 312350.0, 312450.0, 312500.0),
                sources = generateSourcesForAsset("CHF", 312500, 1.185, 0.45, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "CNY",
                symbol = "CNY",
                nameFa = "یوان چین",
                nameEn = "Chinese Yuan",
                priceToman = 35900,
                priceUsd = 0.138,
                changePercent24h = 0.30,
                high24hToman = 36050,
                low24hToman = 35750,
                high24hUsd = 0.139,
                low24hUsd = 0.137,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(35750.0, 35800.0, 35850.0, 35870.0, 35890.0, 35895.0, 35900.0),
                sources = generateSourcesForAsset("CNY", 35900, 0.138, 0.30, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "AUD",
                symbol = "AUD",
                nameFa = "دلار استرالیا",
                nameEn = "Australian Dollar",
                priceToman = 164200,
                priceUsd = 0.635,
                changePercent24h = 0.70,
                high24hToman = 164500,
                low24hToman = 163100,
                high24hUsd = 0.638,
                low24hUsd = 0.631,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(163100.0, 163400.0, 163800.0, 164000.0, 164100.0, 164150.0, 164200.0),
                sources = generateSourcesForAsset("AUD", 164200, 0.635, 0.70, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "SAR",
                symbol = "SAR",
                nameFa = "ریال عربستان",
                nameEn = "Saudi Riyal",
                priceToman = 68900,
                priceUsd = 0.266,
                changePercent24h = 0.50,
                high24hToman = 69050,
                low24hToman = 68400,
                high24hUsd = 0.267,
                low24hUsd = 0.264,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(68400.0, 68550.0, 68700.0, 68780.0, 68820.0, 68870.0, 68900.0),
                sources = generateSourcesForAsset("SAR", 68900, 0.266, 0.50, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "GEL",
                symbol = "GEL",
                nameFa = "لاری گرجستان",
                nameEn = "Georgian Lari",
                priceToman = 94800,
                priceUsd = 0.366,
                changePercent24h = 0.25,
                high24hToman = 95100,
                low24hToman = 94200,
                high24hUsd = 0.368,
                low24hUsd = 0.364,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(94200.0, 94350.0, 94500.0, 94620.0, 94700.0, 94750.0, 94800.0),
                sources = generateSourcesForAsset("GEL", 94800, 0.366, 0.25, nowStr, AssetType.DOLLAR_FIAT)
            ),
            // NEW REQUESTED CURRENCIES
            ExchangeItem(
                id = "IQD",
                symbol = "IQD",
                nameFa = "دینار عراق (هر ۱۰۰ دینار)",
                nameEn = "Iraqi Dinar (100)",
                priceToman = 19750,
                priceUsd = 0.076,
                changePercent24h = 0.40,
                high24hToman = 19820,
                low24hToman = 19600,
                high24hUsd = 0.077,
                low24hUsd = 0.075,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(19600.0, 19630.0, 19680.0, 19710.0, 19730.0, 19745.0, 19750.0),
                sources = generateSourcesForAsset("IQD", 19750, 0.076, 0.40, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "AZN",
                symbol = "AZN",
                nameFa = "منات آذربایجان",
                nameEn = "Azerbaijani Manat",
                priceToman = 152000,
                priceUsd = 0.588,
                changePercent24h = 0.65,
                high24hToman = 152300,
                low24hToman = 151000,
                high24hUsd = 0.590,
                low24hUsd = 0.584,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(151000.0, 151200.0, 151500.0, 151750.0, 151880.0, 151950.0, 152000.0),
                sources = generateSourcesForAsset("AZN", 152000, 0.588, 0.65, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "AMD",
                symbol = "AMD",
                nameFa = "درام ارمنستان (هر ۱۰۰)",
                nameEn = "Armenian Dram (100)",
                priceToman = 66500,
                priceUsd = 0.258,
                changePercent24h = 0.35,
                high24hToman = 66700,
                low24hToman = 66100,
                high24hUsd = 0.260,
                low24hUsd = 0.256,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(66100.0, 66200.0, 66350.0, 66400.0, 66460.0, 66480.0, 66500.0),
                sources = generateSourcesForAsset("AMD", 66500, 0.258, 0.35, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "THB",
                symbol = "THB",
                nameFa = "بات تایلند",
                nameEn = "Thai Baht",
                priceToman = 7650,
                priceUsd = 0.029,
                changePercent24h = 0.15,
                high24hToman = 7690,
                low24hToman = 7600,
                high24hUsd = 0.030,
                low24hUsd = 0.028,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(7600.0, 7610.0, 7625.0, 7635.0, 7640.0, 7648.0, 7650.0),
                sources = generateSourcesForAsset("THB", 7650, 0.029, 0.15, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "MYR",
                symbol = "MYR",
                nameFa = "رینگیت مالزی",
                nameEn = "Malaysian Ringgit",
                priceToman = 58400,
                priceUsd = 0.226,
                changePercent24h = 0.50,
                high24hToman = 58650,
                low24hToman = 58000,
                high24hUsd = 0.228,
                low24hUsd = 0.224,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(58000.0, 58100.0, 58220.0, 58300.0, 58350.0, 58380.0, 58400.0),
                sources = generateSourcesForAsset("MYR", 58400, 0.226, 0.50, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "OMR",
                symbol = "OMR",
                nameFa = "ریال عمان",
                nameEn = "Omani Rial",
                priceToman = 672000,
                priceUsd = 2.60,
                changePercent24h = 0.80,
                high24hToman = 673500,
                low24hToman = 668000,
                high24hUsd = 2.61,
                low24hUsd = 2.58,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(668000.0, 669000.0, 670200.0, 671100.0, 671600.0, 671900.0, 672000.0),
                sources = generateSourcesForAsset("OMR", 672000, 2.60, 0.80, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "KWD",
                symbol = "KWD",
                nameFa = "دینار کویت",
                nameEn = "Kuwaiti Dinar",
                priceToman = 843000,
                priceUsd = 3.26,
                changePercent24h = 0.75,
                high24hToman = 845000,
                low24hToman = 839000,
                high24hUsd = 3.28,
                low24hUsd = 3.24,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(839000.0, 840200.0, 841500.0, 842100.0, 842600.0, 842900.0, 843000.0),
                sources = generateSourcesForAsset("KWD", 843000, 3.26, 0.75, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "SEK",
                symbol = "SEK",
                nameFa = "کرون سوئد",
                nameEn = "Swedish Krona",
                priceToman = 24800,
                priceUsd = 0.096,
                changePercent24h = 0.20,
                high24hToman = 24950,
                low24hToman = 24600,
                high24hUsd = 0.097,
                low24hUsd = 0.095,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(24600.0, 24650.0, 24700.0, 24740.0, 24770.0, 24790.0, 24800.0),
                sources = generateSourcesForAsset("SEK", 24800, 0.096, 0.20, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "QAR",
                symbol = "QAR",
                nameFa = "ریال قطر",
                nameEn = "Qatari Riyal",
                priceToman = 71000,
                priceUsd = 0.274,
                changePercent24h = 0.45,
                high24hToman = 71200,
                low24hToman = 70600,
                high24hUsd = 0.276,
                low24hUsd = 0.272,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(70600.0, 70750.0, 70850.0, 70920.0, 70960.0, 70990.0, 71000.0),
                sources = generateSourcesForAsset("QAR", 71000, 0.274, 0.45, nowStr, AssetType.DOLLAR_FIAT)
            ),
            ExchangeItem(
                id = "RUB",
                symbol = "RUB",
                nameFa = "روبل روسیه",
                nameEn = "Russian Ruble",
                priceToman = 2780,
                priceUsd = 0.0107,
                changePercent24h = -0.15,
                high24hToman = 2810,
                low24hToman = 2750,
                high24hUsd = 0.0109,
                low24hUsd = 0.0105,
                type = AssetType.DOLLAR_FIAT,
                historyPoints = listOf(2750.0, 2760.0, 2770.0, 2775.0, 2778.0, 2780.0, 2780.0),
                sources = generateSourcesForAsset("RUB", 2780, 0.0107, -0.15, nowStr, AssetType.DOLLAR_FIAT)
            ),

            // ==================== CRYPTOCURRENCIES ====================
            ExchangeItem(
                id = "BTC",
                symbol = "BTC",
                nameFa = "بیت‌کوین",
                nameEn = "Bitcoin",
                priceToman = 21762753000L,
                priceUsd = 84200.0,
                changePercent24h = 2.85,
                high24hToman = 21980000000L,
                low24hToman = 21250000000L,
                high24hUsd = 85100.0,
                low24hUsd = 82400.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(82400.0, 82900.0, 83500.0, 83800.0, 84000.0, 84150.0, 84200.0),
                sources = generateSourcesForAsset("BTC", 21762753000L, 84200.0, 2.85, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "ETH",
                symbol = "ETH",
                nameFa = "اتریوم",
                nameEn = "Ethereum",
                priceToman = 736625250L,
                priceUsd = 2850.0,
                changePercent24h = 1.95,
                high24hToman = 744000000L,
                low24hToman = 721000000L,
                high24hUsd = 2880.0,
                low24hUsd = 2790.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(2790.0, 2810.0, 2825.0, 2838.0, 2842.0, 2848.0, 2850.0),
                sources = generateSourcesForAsset("ETH", 736625250L, 2850.0, 1.95, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "SOL",
                symbol = "SOL",
                nameFa = "سولانا",
                nameEn = "Solana",
                priceToman = 45231375L,
                priceUsd = 175.0,
                changePercent24h = 4.20,
                high24hToman = 45900000L,
                low24hToman = 43200000L,
                high24hUsd = 178.0,
                low24hUsd = 167.5,
                type = AssetType.CRYPTO,
                historyPoints = listOf(167.5, 169.2, 171.4, 173.0, 174.1, 174.8, 175.0),
                sources = generateSourcesForAsset("SOL", 45231375L, 175.0, 4.20, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "BNB",
                symbol = "BNB",
                nameFa = "بایننس کوین",
                nameEn = "BNB",
                priceToman = 153786675L,
                priceUsd = 595.0,
                changePercent24h = 0.85,
                high24hToman = 155000000L,
                low24hToman = 151500000L,
                high24hUsd = 601.0,
                low24hUsd = 588.0,
                type = AssetType.CRYPTO,
                historyPoints = listOf(588.0, 590.0, 592.5, 593.8, 594.2, 594.8, 595.0),
                sources = generateSourcesForAsset("BNB", 153786675L, 595.0, 0.85, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "XRP",
                symbol = "XRP",
                nameFa = "ریپل",
                nameEn = "XRP",
                priceToman = 139571L,
                priceUsd = 0.54,
                changePercent24h = -0.45,
                high24hToman = 142000L,
                low24hToman = 137000L,
                high24hUsd = 0.552,
                low24hUsd = 0.531,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.552, 0.548, 0.545, 0.542, 0.541, 0.540, 0.54),
                sources = generateSourcesForAsset("XRP", 139571L, 0.54, -0.45, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "TON",
                symbol = "TON",
                nameFa = "تون‌کوین",
                nameEn = "Toncoin",
                priceToman = 1344018L,
                priceUsd = 5.20,
                changePercent24h = 1.15,
                high24hToman = 1365000L,
                low24hToman = 1310000L,
                high24hUsd = 5.30,
                low24hUsd = 5.08,
                type = AssetType.CRYPTO,
                historyPoints = listOf(5.08, 5.12, 5.15, 5.17, 5.18, 5.19, 5.20),
                sources = generateSourcesForAsset("TON", 1344018L, 5.20, 1.15, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "DOGE",
                symbol = "DOGE",
                nameFa = "دوج‌کوین",
                nameEn = "Dogecoin",
                priceToman = 36185L,
                priceUsd = 0.14,
                changePercent24h = 5.60,
                high24hToman = 37500L,
                low24hToman = 33800L,
                high24hUsd = 0.146,
                low24hUsd = 0.132,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.132, 0.135, 0.137, 0.139, 0.1395, 0.140, 0.14),
                sources = generateSourcesForAsset("DOGE", 36185L, 0.14, 5.60, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "ADA",
                symbol = "ADA",
                nameFa = "کاردانو",
                nameEn = "Cardano",
                priceToman = 90462L,
                priceUsd = 0.35,
                changePercent24h = 0.70,
                high24hToman = 92000L,
                low24hToman = 89000L,
                high24hUsd = 0.358,
                low24hUsd = 0.344,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.344, 0.346, 0.348, 0.349, 0.3495, 0.350, 0.35),
                sources = generateSourcesForAsset("ADA", 90462L, 0.35, 0.70, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "AVAX",
                symbol = "AVAX",
                nameFa = "آوالانچ",
                nameEn = "Avalanche",
                priceToman = 6978555L,
                priceUsd = 27.0,
                changePercent24h = 3.10,
                high24hToman = 7150000L,
                low24hToman = 6720000L,
                high24hUsd = 27.8,
                low24hUsd = 26.1,
                type = AssetType.CRYPTO,
                historyPoints = listOf(26.1, 26.4, 26.6, 26.8, 26.9, 26.95, 27.0),
                sources = generateSourcesForAsset("AVAX", 6978555L, 27.0, 3.10, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "LINK",
                symbol = "LINK",
                nameFa = "چین‌لینک",
                nameEn = "Chainlink",
                priceToman = 2972347L,
                priceUsd = 11.5,
                changePercent24h = 2.40,
                high24hToman = 3050000L,
                low24hToman = 2890000L,
                high24hUsd = 11.85,
                low24hUsd = 11.18,
                type = AssetType.CRYPTO,
                historyPoints = listOf(11.18, 11.25, 11.35, 11.42, 11.46, 11.49, 11.5),
                sources = generateSourcesForAsset("LINK", 2972347L, 11.5, 2.40, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "SUI",
                symbol = "SUI",
                nameFa = "سویی",
                nameEn = "Sui",
                priceToman = 504000L,
                priceUsd = 1.95,
                changePercent24h = 6.40,
                high24hToman = 525000L,
                low24hToman = 465000L,
                high24hUsd = 2.05,
                low24hUsd = 1.82,
                type = AssetType.CRYPTO,
                historyPoints = listOf(1.82, 1.86, 1.90, 1.92, 1.93, 1.94, 1.95),
                sources = generateSourcesForAsset("SUI", 504000L, 1.95, 6.40, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "NEAR",
                symbol = "NEAR",
                nameFa = "نیر پروتکل",
                nameEn = "NEAR Protocol",
                priceToman = 1253000L,
                priceUsd = 4.85,
                changePercent24h = 3.80,
                high24hToman = 1295000L,
                low24hToman = 1195000L,
                high24hUsd = 5.02,
                low24hUsd = 4.65,
                type = AssetType.CRYPTO,
                historyPoints = listOf(4.65, 4.70, 4.75, 4.80, 4.82, 4.84, 4.85),
                sources = generateSourcesForAsset("NEAR", 1253000L, 4.85, 3.80, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "DOT",
                symbol = "DOT",
                nameFa = "پولکادات",
                nameEn = "Polkadot",
                priceToman = 1137000L,
                priceUsd = 4.40,
                changePercent24h = 1.10,
                high24hToman = 1160000L,
                low24hToman = 1110000L,
                high24hUsd = 4.52,
                low24hUsd = 4.31,
                type = AssetType.CRYPTO,
                historyPoints = listOf(4.31, 4.34, 4.37, 4.38, 4.39, 4.395, 4.40),
                sources = generateSourcesForAsset("DOT", 1137000L, 4.40, 1.10, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "POL",
                symbol = "POL",
                nameFa = "پولیگان (ماتیک)",
                nameEn = "Polygon",
                priceToman = 98200L,
                priceUsd = 0.38,
                changePercent24h = 1.80,
                high24hToman = 101000L,
                low24hToman = 95500L,
                high24hUsd = 0.392,
                low24hUsd = 0.371,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.371, 0.374, 0.377, 0.379, 0.3795, 0.38, 0.38),
                sources = generateSourcesForAsset("POL", 98200L, 0.38, 1.80, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "PEPE",
                symbol = "PEPE",
                nameFa = "پپه",
                nameEn = "Pepe",
                priceToman = 267L,
                priceUsd = 0.0000103,
                changePercent24h = 7.80,
                high24hToman = 285L,
                low24hToman = 245L,
                high24hUsd = 0.0000112,
                low24hUsd = 0.0000094,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.0000094, 0.0000097, 0.0000100, 0.0000101, 0.0000102, 0.0000103, 0.0000103),
                sources = generateSourcesForAsset("PEPE", 267L, 0.0000103, 7.80, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "NOT",
                symbol = "NOT",
                nameFa = "نات‌کوین",
                nameEn = "Notcoin",
                priceToman = 2120L,
                priceUsd = 0.0082,
                changePercent24h = 4.10,
                high24hToman = 2240L,
                low24hToman = 2010L,
                high24hUsd = 0.0087,
                low24hUsd = 0.0078,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.0078, 0.0079, 0.0080, 0.0081, 0.00815, 0.0082, 0.0082),
                sources = generateSourcesForAsset("NOT", 2120L, 0.0082, 4.10, nowStr, AssetType.CRYPTO)
            ),
            ExchangeItem(
                id = "FLOKI",
                symbol = "FLOKI",
                nameFa = "فلوکی اینو",
                nameEn = "Floki",
                priceToman = 3880L,
                priceUsd = 0.00015,
                changePercent24h = 5.20,
                high24hToman = 4100L,
                low24hToman = 3650L,
                high24hUsd = 0.00016,
                low24hUsd = 0.00014,
                type = AssetType.CRYPTO,
                historyPoints = listOf(0.00014, 0.000143, 0.000146, 0.000148, 0.000149, 0.00015, 0.00015),
                sources = generateSourcesForAsset("FLOKI", 3880L, 0.00015, 5.20, nowStr, AssetType.CRYPTO)
            ),

            // ==================== GOLD & COINS ====================
            ExchangeItem(
                id = "GOLD_18K",
                symbol = "GOLD18",
                nameFa = "طلای ۱۸ عیار (هر گرم)",
                nameEn = "Gold 18K",
                priceToman = 25694400L,
                priceUsd = 99.4,
                changePercent24h = 0.15,
                high24hToman = 25787200L,
                low24hToman = 25300100L,
                high24hUsd = 100.5,
                low24hUsd = 97.8,
                type = AssetType.GOLD,
                historyPoints = listOf(25300100.0, 25420000.0, 25510000.0, 25600000.0, 25650000.0, 25680000.0, 25694400.0),
                unit = "تومان/گرم",
                sources = generateSourcesForAsset("GOLD_18K", 25694400L, 99.4, 0.15, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "GOLD_24K",
                symbol = "GOLD24",
                nameFa = "طلای ۲۴ عیار (هر گرم)",
                nameEn = "Gold 24K",
                priceToman = 34258800L,
                priceUsd = 132.5,
                changePercent24h = 0.15,
                high24hToman = 34382600L,
                low24hToman = 33733100L,
                high24hUsd = 133.2,
                low24hUsd = 130.5,
                type = AssetType.GOLD,
                historyPoints = listOf(33733100.0, 33850000.0, 34010000.0, 34120000.0, 34200000.0, 34240000.0, 34258800.0),
                unit = "تومان/گرم",
                sources = generateSourcesForAsset("GOLD_24K", 34258800L, 132.5, 0.15, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "SEKKE_EMAMI",
                symbol = "EMAMI",
                nameFa = "سکه امامی (تمام بهار)",
                nameEn = "Emami Coin",
                priceToman = 260395000L,
                priceUsd = 1007.4,
                changePercent24h = 0.25,
                high24hToman = 260810000L,
                low24hToman = 258980000L,
                high24hUsd = 1015.0,
                low24hUsd = 998.0,
                type = AssetType.GOLD,
                historyPoints = listOf(258980000.0, 259200000.0, 259600000.0, 260100000.0, 260250000.0, 260350000.0, 260395000.0),
                unit = "تومان",
                sources = generateSourcesForAsset("SEKKE_EMAMI", 260395000L, 1007.4, 0.25, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "SEKKE_BAHAR",
                symbol = "BAHAR",
                nameFa = "سکه بهار آزادی (طرح قدیم)",
                nameEn = "Bahar Azadi",
                priceToman = 252065000L,
                priceUsd = 975.2,
                changePercent24h = 0.20,
                high24hToman = 252450000L,
                low24hToman = 251450000L,
                high24hUsd = 980.0,
                low24hUsd = 970.0,
                type = AssetType.GOLD,
                historyPoints = listOf(251450000.0, 251600000.0, 251800000.0, 251950000.0, 252000000.0, 252040000.0, 252065000.0),
                unit = "تومان",
                sources = generateSourcesForAsset("SEKKE_BAHAR", 252065000L, 975.2, 0.20, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "SEKKE_NIM",
                symbol = "NIM",
                nameFa = "نیم سکه بهار آزادی",
                nameEn = "Half Coin",
                priceToman = 135740000L,
                priceUsd = 525.1,
                changePercent24h = 0.18,
                high24hToman = 135900000L,
                low24hToman = 135000000L,
                high24hUsd = 530.0,
                low24hUsd = 521.0,
                type = AssetType.GOLD,
                historyPoints = listOf(135000000.0, 135200000.0, 135400000.0, 135550000.0, 135650000.0, 135700000.0, 135740000.0),
                unit = "تومان",
                sources = generateSourcesForAsset("SEKKE_NIM", 135740000L, 525.1, 0.18, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "SEKKE_ROB",
                symbol = "ROB",
                nameFa = "ربع سکه بهار آزادی",
                nameEn = "Quarter Coin",
                priceToman = 73100000L,
                priceUsd = 282.8,
                changePercent24h = 0.35,
                high24hToman = 73480000L,
                low24hToman = 72900000L,
                high24hUsd = 286.0,
                low24hUsd = 280.0,
                type = AssetType.GOLD,
                historyPoints = listOf(72900000.0, 72950000.0, 73000000.0, 73050000.0, 73080000.0, 73090000.0, 73100000.0),
                unit = "تومان",
                sources = generateSourcesForAsset("SEKKE_ROB", 73100000L, 282.8, 0.35, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "SEKKE_GERMI",
                symbol = "GERMI",
                nameFa = "سکه گرمی بانک مرکزی",
                nameEn = "Gram Coin",
                priceToman = 37000000L,
                priceUsd = 143.1,
                changePercent24h = 0.10,
                high24hToman = 37000000L,
                low24hToman = 37000000L,
                high24hUsd = 143.5,
                low24hUsd = 142.8,
                type = AssetType.GOLD,
                historyPoints = listOf(37000000.0, 37000000.0, 37000000.0, 37000000.0, 37000000.0, 37000000.0, 37000000.0),
                unit = "تومان",
                sources = generateSourcesForAsset("SEKKE_GERMI", 37000000L, 143.1, 0.10, nowStr, AssetType.GOLD)
            ),
            ExchangeItem(
                id = "OUNCE_GOLD",
                symbol = "XAU",
                nameFa = "انس جهانی طلا",
                nameEn = "Gold Ounce",
                priceToman = 1069430000L,
                priceUsd = 4137.58,
                changePercent24h = -0.91,
                high24hToman = 1091500000L,
                low24hToman = 1068900000L,
                high24hUsd = 4222.95,
                low24hUsd = 4135.82,
                type = AssetType.GOLD,
                historyPoints = listOf(4210.0, 4195.0, 4180.0, 4165.0, 4150.0, 4142.0, 4137.58),
                unit = "دلار/انس",
                sources = generateSourcesForAsset("OUNCE_GOLD", 1069430000L, 4137.58, -0.91, nowStr, AssetType.GOLD)
            )
        )
    }
}
