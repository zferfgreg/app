package com.example.util

import com.example.data.model.ExchangeItem
import com.example.data.model.HistoricalPoint
import com.example.data.model.TimeFrame
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

object ChartDataGenerator {

    fun generatePoints(item: ExchangeItem, timeFrame: TimeFrame): List<HistoricalPoint> {
        val count = timeFrame.count
        val now = System.currentTimeMillis()
        val currentPriceToman = item.priceToman.toDouble()
        val currentPriceUsd = item.priceUsd

        val periodMillis = when (timeFrame) {
            TimeFrame.H1 -> 3600 * 1000L
            TimeFrame.H24 -> 24L * 3600 * 1000L
            TimeFrame.D7 -> 7L * 24 * 3600 * 1000L
            TimeFrame.M1 -> 30L * 24 * 3600 * 1000L
            TimeFrame.M3 -> 90L * 24 * 3600 * 1000L
            TimeFrame.Y1 -> 365L * 24 * 3600 * 1000L
            TimeFrame.ALL -> 3 * 365L * 24 * 3600 * 1000L
        }

        val stepMillis = periodMillis / (count - 1).coerceAtLeast(1)
        val seed = item.id.hashCode() + timeFrame.ordinal * 1000
        val rng = Random(seed)

        // General overall trend slope based on 24h change and timeframe
        val trendSlope = when (timeFrame) {
            TimeFrame.H1 -> (item.changePercent24h / 24.0) / 100.0
            TimeFrame.H24 -> item.changePercent24h / 100.0
            TimeFrame.D7 -> item.changePercent24h * 1.8 / 100.0
            TimeFrame.M1 -> (item.changePercent24h * 3.5 + 4.0) / 100.0
            TimeFrame.M3 -> (item.changePercent24h * 5.0 + 8.5) / 100.0
            TimeFrame.Y1 -> 0.28 // General yearly growth
            TimeFrame.ALL -> 0.85 // Multi-year trend
        }

        val timeFormat = when (timeFrame) {
            TimeFrame.H1 -> SimpleDateFormat("HH:mm", Locale.getDefault())
            TimeFrame.H24 -> SimpleDateFormat("HH:mm", Locale.getDefault())
            TimeFrame.D7 -> SimpleDateFormat("E HH:mm", Locale.forLanguageTag("fa"))
            TimeFrame.M1, TimeFrame.M3 -> SimpleDateFormat("d MMM", Locale.forLanguageTag("fa"))
            TimeFrame.Y1, TimeFrame.ALL -> SimpleDateFormat("MMM yyyy", Locale.forLanguageTag("fa"))
        }

        // Random walk backwards so that last point equals exactly current price
        val rawValues = DoubleArray(count)
        rawValues[count - 1] = 1.0

        val volatility = when (timeFrame) {
            TimeFrame.H1 -> 0.005
            TimeFrame.H24 -> 0.015
            TimeFrame.D7 -> 0.025
            TimeFrame.M1 -> 0.035
            TimeFrame.M3 -> 0.045
            TimeFrame.Y1 -> 0.06
            TimeFrame.ALL -> 0.08
        }

        for (i in count - 2 downTo 0) {
            val progress = (count - 1 - i).toDouble() / count.toDouble()
            val noise = rng.nextDouble(-volatility, volatility)
            val wave = sin(i.toDouble() * 0.6) * (volatility * 0.7)
            val prev = rawValues[i + 1]
            val expected = 1.0 - (progress * trendSlope)
            val nextVal = prev * (1.0 - (trendSlope / count) + noise + wave)
            rawValues[i] = nextVal.coerceIn(expected * 0.70, expected * 1.45)
        }

        // Normalize so rawValues[count - 1] is exactly 1.0
        val lastMultiplier = rawValues[count - 1]
        for (i in 0 until count) {
            rawValues[i] = rawValues[i] / lastMultiplier
        }

        val rawPoints = mutableListOf<HistoricalPoint>()
        var prevClose = (currentPriceToman * rawValues[0] * 0.995).toLong().coerceAtLeast(1L)

        for (i in 0 until count) {
            val timestamp = now - periodMillis + (i * stepMillis)
            val factor = rawValues[i]
            val closeT = (currentPriceToman * factor).toLong().coerceAtLeast(1L)
            val openT = if (i == 0) (closeT * (1.0 - rng.nextDouble(-0.01, 0.01))).toLong().coerceAtLeast(1L) else prevClose
            prevClose = closeT

            val bodyMax = max(openT, closeT)
            val bodyMin = min(openT, closeT)
            val spread = (bodyMax - bodyMin).coerceAtLeast((closeT * 0.002).toLong().coerceAtLeast(1L))

            val highT = bodyMax + (spread * rng.nextDouble(0.2, 1.2)).toLong()
            val lowT = (bodyMin - (spread * rng.nextDouble(0.2, 1.2)).toLong()).coerceAtLeast(1L)

            val priceU = (currentPriceUsd * factor).coerceAtLeast(0.000001)
            val volumeNorm = (0.25f + rng.nextFloat() * 0.65f) * (0.8f + (sin(i.toDouble() * 0.8).toFloat() * 0.2f))

            rawPoints.add(
                HistoricalPoint(
                    timestamp = timestamp,
                    timeLabel = timeFormat.format(Date(timestamp)),
                    priceToman = closeT,
                    priceUsd = priceU,
                    volumeNormalized = volumeNorm.coerceIn(0.12f, 1.0f),
                    openToman = openT,
                    highToman = highT,
                    lowToman = lowT,
                    closeToman = closeT
                )
            )
        }

        // Calculate Moving Averages (MA7 & MA25)
        val finalPoints = rawPoints.mapIndexed { index, point ->
            val ma7Window = rawPoints.subList(max(0, index - 6), index + 1)
            val ma7 = (ma7Window.map { it.priceToman }.average()).toLong()

            val ma25Window = rawPoints.subList(max(0, index - 24), index + 1)
            val ma25 = (ma25Window.map { it.priceToman }.average()).toLong()

            point.copy(
                ma7Toman = ma7,
                ma25Toman = ma25
            )
        }

        return finalPoints
    }
}
