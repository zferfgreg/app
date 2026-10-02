package com.example.util

import com.example.data.model.ExchangeItem
import com.example.data.model.HistoricalPoint
import com.example.data.model.TimeFrame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

object ChartDataGenerator {

    fun generatePoints(item: ExchangeItem, timeFrame: TimeFrame): List<HistoricalPoint> {
        val count = timeFrame.count
        val now = System.currentTimeMillis()
        val currentPriceToman = item.priceToman.toDouble()
        val currentPriceUsd = item.priceUsd

        val periodMillis = when (timeFrame) {
            TimeFrame.H24 -> 24L * 3600 * 1000
            TimeFrame.D7 -> 7L * 24 * 3600 * 1000
            TimeFrame.M1 -> 30L * 24 * 3600 * 1000
            TimeFrame.M3 -> 90L * 24 * 3600 * 1000
            TimeFrame.Y1 -> 365L * 24 * 3600 * 1000
        }

        val stepMillis = periodMillis / (count - 1)
        val seed = item.id.hashCode() + timeFrame.ordinal * 1000
        val rng = Random(seed)

        // General overall trend slope based on 24h change and timeframe
        val trendSlope = when (timeFrame) {
            TimeFrame.H24 -> item.changePercent24h / 100.0
            TimeFrame.D7 -> item.changePercent24h * 1.8 / 100.0
            TimeFrame.M1 -> (item.changePercent24h * 3.5 + 4.0) / 100.0
            TimeFrame.M3 -> (item.changePercent24h * 5.0 + 8.5) / 100.0
            TimeFrame.Y1 -> 0.28 // general yearly inflation/crypto growth
        }

        val points = mutableListOf<HistoricalPoint>()
        val timeFormat = when (timeFrame) {
            TimeFrame.H24 -> SimpleDateFormat("HH:mm", Locale.getDefault())
            TimeFrame.D7 -> SimpleDateFormat("E HH:mm", Locale("fa"))
            TimeFrame.M1, TimeFrame.M3 -> SimpleDateFormat("d MMM", Locale("fa"))
            TimeFrame.Y1 -> SimpleDateFormat("MMM yyyy", Locale("fa"))
        }

        // We build backwards or forward such that the last point (index count-1) exactly equals current price
        val rawValues = DoubleArray(count)
        rawValues[count - 1] = 1.0

        for (i in count - 2 downTo 0) {
            val progress = (count - 1 - i).toDouble() / count.toDouble()
            // Random walk with mean reversion and slight wave
            val noise = rng.nextDouble(-0.018, 0.018)
            val wave = sin(i.toDouble() * 0.7) * 0.015
            val prev = rawValues[i + 1]
            val expected = 1.0 - (progress * trendSlope)
            val nextVal = prev * (1.0 - (trendSlope / count) + noise + wave)
            rawValues[i] = nextVal.coerceIn(expected * 0.75, expected * 1.35)
        }

        // Normalize so rawValues[count - 1] is exactly 1.0
        val lastMultiplier = rawValues[count - 1]
        for (i in 0 until count) {
            rawValues[i] = rawValues[i] / lastMultiplier
        }

        for (i in 0 until count) {
            val timestamp = now - periodMillis + (i * stepMillis)
            val factor = rawValues[i]
            val priceT = (currentPriceToman * factor).toLong().coerceAtLeast(1L)
            val priceU = (currentPriceUsd * factor).coerceAtLeast(0.000001)
            val volumeNorm = (0.2f + rng.nextFloat() * 0.7f) * (0.8f + (sin(i.toDouble()).toFloat() * 0.2f))

            points.add(
                HistoricalPoint(
                    timestamp = timestamp,
                    timeLabel = timeFormat.format(Date(timestamp)),
                    priceToman = priceT,
                    priceUsd = priceU,
                    volumeNormalized = volumeNorm.coerceIn(0.1f, 1.0f)
                )
            )
        }

        return points
    }
}
