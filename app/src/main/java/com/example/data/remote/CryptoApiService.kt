package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RemoteCryptoQuote(
    val id: String,
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double
)

class CryptoApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLiveCryptoRates(): List<RemoteCryptoQuote> = withContext(Dispatchers.IO) {
        val results = mutableListOf<RemoteCryptoQuote>()
        
        // Attempt CoinCap API first (no API key required)
        try {
            val request = Request.Builder()
                .url("https://api.coincap.io/v2/assets?limit=25")
                .header("User-Agent", "EXCHANCE-Android-App")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val json = JSONObject(bodyString)
                        val dataArray = json.optJSONArray("data")
                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                val symbol = item.optString("symbol", "").uppercase()
                                val name = item.optString("name", "")
                                val price = item.optString("priceUsd", "0").toDoubleOrNull() ?: 0.0
                                val change = item.optString("changePercent24Hr", "0").toDoubleOrNull() ?: 0.0
                                val vwap24Hr = item.optString("vwap24Hr", "0").toDoubleOrNull() ?: price
                                
                                val high = if (change >= 0) price * (1.0 + Math.abs(change) / 200.0) else price * 1.02
                                val low = if (change < 0) price * (1.0 - Math.abs(change) / 200.0) else price * 0.98

                                results.add(
                                    RemoteCryptoQuote(
                                        id = symbol,
                                        symbol = symbol,
                                        name = name,
                                        priceUsd = price,
                                        changePercent24h = change,
                                        high24h = high,
                                        low24h = low
                                    )
                                )
                            }
                            if (results.isNotEmpty()) {
                                return@withContext results
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("CryptoApiService", "CoinCap fetch failed: ${e.message}")
        }

        // Secondary Fallback: Binance 24hr ticker API (free, reliable, public)
        try {
            val request = Request.Builder()
                .url("https://api.binance.com/api/v3/ticker/24hr")
                .header("User-Agent", "EXCHANCE-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val symbolsOfInterest = setOf(
                            "BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT",
                            "XRPUSDT", "ADAUSDT", "DOGEUSDT", "TONUSDT",
                            "TRXUSDT", "AVAXUSDT", "SHIBUSDT"
                        )
                        val jsonArray = org.json.JSONArray(bodyString)
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val pair = item.optString("symbol", "")
                            if (symbolsOfInterest.contains(pair)) {
                                val cleanSymbol = pair.removeSuffix("USDT")
                                val price = item.optString("lastPrice", "0").toDoubleOrNull() ?: 0.0
                                val change = item.optString("priceChangePercent", "0").toDoubleOrNull() ?: 0.0
                                val high = item.optString("highPrice", "0").toDoubleOrNull() ?: price
                                val low = item.optString("lowPrice", "0").toDoubleOrNull() ?: price

                                results.add(
                                    RemoteCryptoQuote(
                                        id = cleanSymbol,
                                        symbol = cleanSymbol,
                                        name = cleanSymbol,
                                        priceUsd = price,
                                        changePercent24h = change,
                                        high24h = high,
                                        low24h = low
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("CryptoApiService", "Binance fallback fetch failed: ${e.message}")
        }

        results
    }
}
