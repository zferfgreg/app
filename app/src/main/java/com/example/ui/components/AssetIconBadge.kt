package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechPurple
import com.example.ui.theme.FintechRed

@Composable
fun AssetIconBadge(
    item: ExchangeItem,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp
) {
    AssetIconBadge(
        symbol = item.symbol,
        type = item.type,
        size = size,
        modifier = modifier
    )
}

@Composable
fun AssetIconBadge(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    type: AssetType? = null
) {
    val upperSym = symbol.uppercase()
    val badgeData = getBadgeVisual(upperSym, type)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(badgeData.bgBrush)
            .border(1.dp, badgeData.borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (badgeData.isEmoji) {
            Text(
                text = badgeData.displayText,
                fontSize = (size.value * 0.48f).sp
            )
        } else {
            Text(
                text = badgeData.displayText,
                fontSize = (size.value * 0.36f).sp,
                fontWeight = FontWeight.Black,
                color = badgeData.textColor
            )
        }
    }
}

private data class BadgeVisual(
    val displayText: String,
    val isEmoji: Boolean,
    val bgBrush: Brush,
    val borderColor: Color,
    val textColor: Color
)

private fun getBadgeVisual(symbol: String, type: AssetType?): BadgeVisual {
    return when {
        // Gold & Coins
        symbol.contains("GOLD") || symbol.contains("SEKKE") || symbol.contains("BAHAR") || symbol.contains("GERAM") || symbol.contains("NIM") || symbol.contains("ROB") -> {
            val emoji = if (symbol.contains("SEKKE") || symbol.contains("BAHAR")) "🪙" else "🥇"
            BadgeVisual(
                displayText = emoji,
                isEmoji = true,
                bgBrush = Brush.radialGradient(listOf(FintechGold.copy(alpha = 0.25f), FintechGold.copy(alpha = 0.08f))),
                borderColor = FintechGold.copy(alpha = 0.4f),
                textColor = FintechGold
            )
        }
        // Currencies
        symbol == "USD" -> BadgeVisual("🇺🇸", true, Brush.radialGradient(listOf(FintechGreen.copy(alpha = 0.25f), FintechGreen.copy(alpha = 0.05f))), FintechGreen.copy(alpha = 0.35f), FintechGreen)
        symbol == "EUR" -> BadgeVisual("🇪🇺", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)
        symbol == "GBP" -> BadgeVisual("🇬🇧", true, Brush.radialGradient(listOf(FintechPurple.copy(alpha = 0.25f), FintechPurple.copy(alpha = 0.05f))), FintechPurple.copy(alpha = 0.35f), FintechPurple)
        symbol == "AED" -> BadgeVisual("🇦🇪", true, Brush.radialGradient(listOf(FintechGold.copy(alpha = 0.25f), FintechGold.copy(alpha = 0.05f))), FintechGold.copy(alpha = 0.35f), FintechGold)
        symbol == "CAD" -> BadgeVisual("🇨🇦", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "TRY" -> BadgeVisual("🇹🇷", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "CNY" -> BadgeVisual("🇨🇳", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "JPY" -> BadgeVisual("🇯🇵", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "CHF" -> BadgeVisual("🇨🇭", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "AUD" -> BadgeVisual("🇦🇺", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)
        symbol == "SAR" -> BadgeVisual("🇸🇦", true, Brush.radialGradient(listOf(FintechGreen.copy(alpha = 0.25f), FintechGreen.copy(alpha = 0.05f))), FintechGreen.copy(alpha = 0.35f), FintechGreen)
        symbol == "IQD" -> BadgeVisual("🇮🇶", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "OMR" -> BadgeVisual("🇴🇲", true, Brush.radialGradient(listOf(FintechGreen.copy(alpha = 0.25f), FintechGreen.copy(alpha = 0.05f))), FintechGreen.copy(alpha = 0.35f), FintechGreen)
        symbol == "KWD" -> BadgeVisual("🇰🇼", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)
        symbol == "QAR" -> BadgeVisual("🇶🇦", true, Brush.radialGradient(listOf(FintechPurple.copy(alpha = 0.25f), FintechPurple.copy(alpha = 0.05f))), FintechPurple.copy(alpha = 0.35f), FintechPurple)
        symbol == "RUB" -> BadgeVisual("🇷🇺", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)
        symbol == "AZN" -> BadgeVisual("🇦🇿", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)
        symbol == "AMD" -> BadgeVisual("🇦🇲", true, Brush.radialGradient(listOf(FintechGold.copy(alpha = 0.25f), FintechGold.copy(alpha = 0.05f))), FintechGold.copy(alpha = 0.35f), FintechGold)
        symbol == "GEL" -> BadgeVisual("🇬🇪", true, Brush.radialGradient(listOf(FintechRed.copy(alpha = 0.25f), FintechRed.copy(alpha = 0.05f))), FintechRed.copy(alpha = 0.35f), FintechRed)
        symbol == "THB" -> BadgeVisual("🇹🇭", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)
        symbol == "MYR" -> BadgeVisual("🇲🇾", true, Brush.radialGradient(listOf(FintechGold.copy(alpha = 0.25f), FintechGold.copy(alpha = 0.05f))), FintechGold.copy(alpha = 0.35f), FintechGold)
        symbol == "SEK" -> BadgeVisual("🇸🇪", true, Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.25f), FintechCyan.copy(alpha = 0.05f))), FintechCyan.copy(alpha = 0.35f), FintechCyan)

        // Cryptocurrencies
        symbol == "BTC" -> BadgeVisual("₿", false, Brush.radialGradient(listOf(Color(0xFFF7931A).copy(alpha = 0.3f), Color(0xFFF7931A).copy(alpha = 0.08f))), Color(0xFFF7931A).copy(alpha = 0.5f), Color(0xFFF7931A))
        symbol == "ETH" -> BadgeVisual("Ξ", false, Brush.radialGradient(listOf(Color(0xFF627EEA).copy(alpha = 0.3f), Color(0xFF627EEA).copy(alpha = 0.08f))), Color(0xFF627EEA).copy(alpha = 0.5f), Color(0xFF8C9EFF))
        symbol == "USDT" -> BadgeVisual("₮", false, Brush.radialGradient(listOf(Color(0xFF26A17B).copy(alpha = 0.3f), Color(0xFF26A17B).copy(alpha = 0.08f))), Color(0xFF26A17B).copy(alpha = 0.5f), Color(0xFF26A17B))
        symbol == "SOL" -> BadgeVisual("SOL", false, Brush.radialGradient(listOf(Color(0xFF14F195).copy(alpha = 0.25f), Color(0xFF9945FF).copy(alpha = 0.15f))), Color(0xFF9945FF).copy(alpha = 0.5f), Color(0xFF14F195))
        symbol == "BNB" -> BadgeVisual("BNB", false, Brush.radialGradient(listOf(Color(0xFFF3BA2F).copy(alpha = 0.3f), Color(0xFFF3BA2F).copy(alpha = 0.08f))), Color(0xFFF3BA2F).copy(alpha = 0.5f), Color(0xFFF3BA2F))
        symbol == "XRP" -> BadgeVisual("XRP", false, Brush.radialGradient(listOf(Color(0xFF23292F).copy(alpha = 0.4f), FintechCyan.copy(alpha = 0.15f))), FintechCyan.copy(alpha = 0.4f), FintechCyan)
        symbol == "DOGE" -> BadgeVisual("🐕", true, Brush.radialGradient(listOf(Color(0xFFC2A633).copy(alpha = 0.3f), Color(0xFFC2A633).copy(alpha = 0.08f))), Color(0xFFC2A633).copy(alpha = 0.5f), Color(0xFFC2A633))
        symbol == "TON" -> BadgeVisual("TON", false, Brush.radialGradient(listOf(Color(0xFF0088CC).copy(alpha = 0.3f), Color(0xFF0088CC).copy(alpha = 0.08f))), Color(0xFF0088CC).copy(alpha = 0.5f), Color(0xFF0088CC))
        symbol == "ADA" -> BadgeVisual("ADA", false, Brush.radialGradient(listOf(Color(0xFF0033AD).copy(alpha = 0.3f), FintechCyan.copy(alpha = 0.1f))), FintechCyan.copy(alpha = 0.4f), FintechCyan)
        symbol == "TRX" -> BadgeVisual("TRX", false, Brush.radialGradient(listOf(Color(0xFFFF060A).copy(alpha = 0.3f), Color(0xFFFF060A).copy(alpha = 0.08f))), Color(0xFFFF060A).copy(alpha = 0.5f), Color(0xFFFF5252))
        symbol == "AVAX" -> BadgeVisual("AVAX", false, Brush.radialGradient(listOf(Color(0xFFE84142).copy(alpha = 0.3f), Color(0xFFE84142).copy(alpha = 0.08f))), Color(0xFFE84142).copy(alpha = 0.5f), Color(0xFFE84142))
        symbol == "SHIB" -> BadgeVisual("🐶", true, Brush.radialGradient(listOf(Color(0xFFFFA409).copy(alpha = 0.3f), Color(0xFFFFA409).copy(alpha = 0.08f))), Color(0xFFFFA409).copy(alpha = 0.5f), Color(0xFFFFA409))
        symbol == "DOT" -> BadgeVisual("DOT", false, Brush.radialGradient(listOf(Color(0xFFE6007A).copy(alpha = 0.3f), Color(0xFFE6007A).copy(alpha = 0.08f))), Color(0xFFE6007A).copy(alpha = 0.5f), Color(0xFFFF4081))
        symbol == "LINK" -> BadgeVisual("LINK", false, Brush.radialGradient(listOf(Color(0xFF375BD2).copy(alpha = 0.3f), Color(0xFF375BD2).copy(alpha = 0.08f))), Color(0xFF375BD2).copy(alpha = 0.5f), Color(0xFF82B1FF))

        else -> {
            val text = if (symbol.length > 4) symbol.take(3) else symbol
            BadgeVisual(
                displayText = text,
                isEmoji = false,
                bgBrush = Brush.radialGradient(listOf(FintechCyan.copy(alpha = 0.2f), FintechCyan.copy(alpha = 0.05f))),
                borderColor = FintechCyan.copy(alpha = 0.3f),
                textColor = FintechCyan
            )
        }
    }
}
