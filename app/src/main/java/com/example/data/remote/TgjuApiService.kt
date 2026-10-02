package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TgjuItemQuote(
    val key: String,
    val priceRials: Long,
    val priceUsd: Double,
    val changePercent: Double,
    val highRials: Long,
    val lowRials: Long,
    val highUsd: Double,
    val lowUsd: Double,
    val timeFa: String
)

class TgjuApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun fetchTgjuRates(): Map<String, TgjuItemQuote> = withContext(Dispatchers.IO) {
        val quotes = mutableMapOf<String, TgjuItemQuote>()
        try {
            val request = Request.Builder()
                .url("https://call.tgju.org/ajax.json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Referer", "https://www.tgju.org/")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val root = JSONObject(body)
                        val current = root.optJSONObject("current")
                        if (current != null) {
                            val keys = current.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val node = current.optJSONObject(key) ?: continue
                                val pRaw = node.optString("p", "0").replace(",", "").trim()
                                val hRaw = node.optString("h", "0").replace(",", "").trim()
                                val lRaw = node.optString("l", "0").replace(",", "").trim()
                                val dpRaw = node.optDouble("dp", 0.0)
                                val dt = node.optString("dt", "")
                                val time = node.optString("t", "")

                                val change = if (dt.equals("low", ignoreCase = true)) -Math.abs(dpRaw) else Math.abs(dpRaw)

                                val priceDouble = pRaw.toDoubleOrNull() ?: 0.0
                                val highDouble = hRaw.toDoubleOrNull() ?: priceDouble
                                val lowDouble = lRaw.toDoubleOrNull() ?: priceDouble

                                quotes[key] = TgjuItemQuote(
                                    key = key,
                                    priceRials = priceDouble.toLong(),
                                    priceUsd = priceDouble,
                                    changePercent = change,
                                    highRials = highDouble.toLong(),
                                    lowRials = lowDouble.toLong(),
                                    highUsd = highDouble,
                                    lowUsd = lowDouble,
                                    timeFa = time
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("TgjuApiService", "Failed to fetch from TGJU: ${e.message}")
        }
        quotes
    }
}
