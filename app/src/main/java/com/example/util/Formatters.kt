package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Formatters {
    private val decimalSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val integerFormat = DecimalFormat("#,###", decimalSymbols)
    private val currencyFormat = DecimalFormat("#,##0.00", decimalSymbols)
    private val cryptoPrecisionFormat = DecimalFormat("#,##0.0000", decimalSymbols)
    private val microCryptoFormat = DecimalFormat("0.000000", decimalSymbols)

    fun formatToman(value: Long, inRials: Boolean = false): String {
        val actualValue = if (inRials) value * 10 else value
        val unit = if (inRials) "ریال" else "تومان"
        return "${integerFormat.format(actualValue)} $unit"
    }

    fun formatUsd(price: Double): String {
        return when {
            price >= 100 -> "$${integerFormat.format(price.toLong())}"
            price >= 1.0 -> "$${currencyFormat.format(price)}"
            price >= 0.001 -> "$${cryptoPrecisionFormat.format(price)}"
            else -> "$${microCryptoFormat.format(price)}"
        }
    }

    fun formatPercent(change: Double): String {
        val sign = if (change > 0) "+" else ""
        return "$sign${String.format(Locale.US, "%.2f", change)}%"
    }

    fun formatCompactNumber(value: Double): String {
        return when {
            value >= 1_000_000_000_000.0 -> String.format(Locale.US, "%.2fT", value / 1_000_000_000_000.0)
            value >= 1_000_000_000.0 -> String.format(Locale.US, "%.2fB", value / 1_000_000_000.0)
            value >= 1_000_000.0 -> String.format(Locale.US, "%.2fM", value / 1_000_000.0)
            value >= 1_000.0 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
            else -> String.format(Locale.US, "%.2f", value)
        }
    }

    fun formatCompact(value: Long): String = formatCompactNumber(value.toDouble())
}
