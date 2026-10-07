package com.example.widget

import android.content.Context
import com.example.data.model.ExchangeItem
import com.example.data.remote.CryptoApiService
import com.example.data.remote.TgjuApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WidgetRates(
    val usdPrice: Long = 263200L,
    val usdChange: Double = 2.14,
    val usdHigh: Long = 268500L,
    val usdLow: Long = 261660L,
    val usdtPrice: Long = 264000L,
    val usdtChange: Double = 1.25,
    val goldPrice: Long = 26215000L,
    val goldChange: Double = -2.00,
    val goldHigh: Long = 26740000L,
    val goldLow: Long = 26131000L,
    val coinPrice: Long = 267300000L,
    val coinChange: Double = -1.72,
    val btcUsd: Double = 83178.0,
    val btcChange: Double = 2.95,
    val ounceUsd: Double = 4082.0,
    val lastUpdated: String = "لحظاتی پیش"
)

object WidgetRatesManager {
    private const val PREFS_NAME = "widget_market_rates"
    private val tgjuService = TgjuApiService()
    private val cryptoService = CryptoApiService()

    fun getRates(context: Context): WidgetRates {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultRates = WidgetRates()

        return WidgetRates(
            usdPrice = prefs.getLong("usd_price", defaultRates.usdPrice),
            usdChange = prefs.getFloat("usd_change", defaultRates.usdChange.toFloat()).toDouble(),
            usdHigh = prefs.getLong("usd_high", defaultRates.usdHigh),
            usdLow = prefs.getLong("usd_low", defaultRates.usdLow),
            usdtPrice = prefs.getLong("usdt_price", defaultRates.usdtPrice),
            usdtChange = prefs.getFloat("usdt_change", defaultRates.usdtChange.toFloat()).toDouble(),
            goldPrice = prefs.getLong("gold_price", defaultRates.goldPrice),
            goldChange = prefs.getFloat("gold_change", defaultRates.goldChange.toFloat()).toDouble(),
            goldHigh = prefs.getLong("gold_high", defaultRates.goldHigh),
            goldLow = prefs.getLong("gold_low", defaultRates.goldLow),
            coinPrice = prefs.getLong("coin_price", defaultRates.coinPrice),
            coinChange = prefs.getFloat("coin_change", defaultRates.coinChange.toFloat()).toDouble(),
            btcUsd = prefs.getFloat("btc_usd", defaultRates.btcUsd.toFloat()).toDouble(),
            btcChange = prefs.getFloat("btc_change", defaultRates.btcChange.toFloat()).toDouble(),
            ounceUsd = prefs.getFloat("ounce_usd", defaultRates.ounceUsd.toFloat()).toDouble(),
            lastUpdated = prefs.getString("last_updated", defaultRates.lastUpdated) ?: defaultRates.lastUpdated
        )
    }

    fun saveRatesFromItems(context: Context, items: List<ExchangeItem>) {
        if (items.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val usd = items.find { it.id == "USD" }
        val usdt = items.find { it.id == "USDT" }
        val gold = items.find { it.id == "GOLD_18K" }
        val coin = items.find { it.id == "SEKKE_EMAMI" }
        val btc = items.find { it.id == "BTC" }
        val ounce = items.find { it.id == "OUNCE_GOLD" }

        usd?.let {
            editor.putLong("usd_price", it.priceToman)
            editor.putFloat("usd_change", it.changePercent24h.toFloat())
            if (it.high24hToman > 0) editor.putLong("usd_high", it.high24hToman)
            if (it.low24hToman > 0) editor.putLong("usd_low", it.low24hToman)
        }
        usdt?.let {
            editor.putLong("usdt_price", it.priceToman)
            editor.putFloat("usdt_change", it.changePercent24h.toFloat())
        }
        gold?.let {
            editor.putLong("gold_price", it.priceToman)
            editor.putFloat("gold_change", it.changePercent24h.toFloat())
            if (it.high24hToman > 0) editor.putLong("gold_high", it.high24hToman)
            if (it.low24hToman > 0) editor.putLong("gold_low", it.low24hToman)
        }
        coin?.let {
            editor.putLong("coin_price", it.priceToman)
            editor.putFloat("coin_change", it.changePercent24h.toFloat())
        }
        btc?.let {
            editor.putFloat("btc_usd", it.priceUsd.toFloat())
            editor.putFloat("btc_change", it.changePercent24h.toFloat())
        }
        ounce?.let {
            editor.putFloat("ounce_usd", it.priceUsd.toFloat())
        }

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        editor.putString("last_updated", timeStr)
        editor.apply()
    }

    suspend fun fetchAndStoreLiveRates(context: Context): WidgetRates = withContext(Dispatchers.IO) {
        try {
            val tgjuQuotes = tgjuService.fetchTgjuRates()
            val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val currentRates = getRates(context)

            var newUsdPrice = currentRates.usdPrice
            var newUsdChange = currentRates.usdChange
            var newUsdHigh = currentRates.usdHigh
            var newUsdLow = currentRates.usdLow

            var newGoldPrice = currentRates.goldPrice
            var newGoldChange = currentRates.goldChange
            var newGoldHigh = currentRates.goldHigh
            var newGoldLow = currentRates.goldLow

            var newCoinPrice = currentRates.coinPrice
            var newCoinChange = currentRates.coinChange

            var newUsdtPrice = currentRates.usdtPrice
            var newBtcUsd = currentRates.btcUsd
            var newOunceUsd = currentRates.ounceUsd

            val dollarQuote = tgjuQuotes["price_dollar_rl"]
            if (dollarQuote != null && dollarQuote.priceRials > 0) {
                newUsdPrice = dollarQuote.priceRials / 10L
                newUsdChange = dollarQuote.changePercent
                if (dollarQuote.highRials > 0) newUsdHigh = dollarQuote.highRials / 10L
                if (dollarQuote.lowRials > 0) newUsdLow = dollarQuote.lowRials / 10L
            }

            val goldQuote = tgjuQuotes["geram18"]
            if (goldQuote != null && goldQuote.priceRials > 0) {
                newGoldPrice = goldQuote.priceRials / 10L
                newGoldChange = goldQuote.changePercent
                if (goldQuote.highRials > 0) newGoldHigh = goldQuote.highRials / 10L
                if (goldQuote.lowRials > 0) newGoldLow = goldQuote.lowRials / 10L
            }

            val coinQuote = tgjuQuotes["sekee"]
            if (coinQuote != null && coinQuote.priceRials > 0) {
                newCoinPrice = coinQuote.priceRials / 10L
                newCoinChange = coinQuote.changePercent
            }

            val tetherQuote = tgjuQuotes["crypto-tether-irr"]
            if (tetherQuote != null && tetherQuote.priceRials > 0) {
                newUsdtPrice = tetherQuote.priceRials / 10L
            } else {
                newUsdtPrice = (newUsdPrice * 1.002).toLong()
            }

            val btcQuote = tgjuQuotes["crypto-bitcoin"]
            if (btcQuote != null && btcQuote.priceUsd > 0) {
                newBtcUsd = btcQuote.priceUsd
            }

            val onsQuote = tgjuQuotes["ons"]
            if (onsQuote != null && onsQuote.priceUsd > 0) {
                newOunceUsd = onsQuote.priceUsd
            }

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putLong("usd_price", newUsdPrice)
                putFloat("usd_change", newUsdChange.toFloat())
                putLong("usd_high", newUsdHigh)
                putLong("usd_low", newUsdLow)
                putLong("usdt_price", newUsdtPrice)
                putLong("gold_price", newGoldPrice)
                putFloat("gold_change", newGoldChange.toFloat())
                putLong("gold_high", newGoldHigh)
                putLong("gold_low", newGoldLow)
                putLong("coin_price", newCoinPrice)
                putFloat("coin_change", newCoinChange.toFloat())
                putFloat("btc_usd", newBtcUsd.toFloat())
                putFloat("ounce_usd", newOunceUsd.toFloat())
                putString("last_updated", timeStr)
                apply()
            }

            return@withContext WidgetRates(
                usdPrice = newUsdPrice,
                usdChange = newUsdChange,
                usdHigh = newUsdHigh,
                usdLow = newUsdLow,
                usdtPrice = newUsdtPrice,
                usdtChange = currentRates.usdtChange,
                goldPrice = newGoldPrice,
                goldChange = newGoldChange,
                goldHigh = newGoldHigh,
                goldLow = newGoldLow,
                coinPrice = newCoinPrice,
                coinChange = newCoinChange,
                btcUsd = newBtcUsd,
                btcChange = currentRates.btcChange,
                ounceUsd = newOunceUsd,
                lastUpdated = timeStr
            )
        } catch (e: Exception) {
            getRates(context)
        }
    }
}
