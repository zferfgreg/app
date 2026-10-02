package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechIndigo
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun ExchangeItemCard(
    item: ExchangeItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = item.changePercent24h >= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("exchange_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Price and 24h change
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1.3f)
            ) {
                Text(
                    text = Formatters.formatToman(item.priceToman),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Formatters.formatUsd(item.priceUsd),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Change badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPositive) FintechGreenBg else FintechRedBg)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isPositive) FintechGreen else FintechRed,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = Formatters.formatPercent(item.changePercent24h),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isPositive) FintechGreen else FintechRed
                            )
                        }
                    }
                }
            }

            // Center: Mini Sparkline Chart
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(34.dp)
                    .padding(horizontal = 6.dp)
            ) {
                SparklineChart(
                    points = item.historyPoints,
                    isPositive = isPositive,
                    modifier = Modifier.matchParentSize(),
                    showGradient = true,
                    strokeWidth = 3f
                )
            }

            // Right Column: Name, Symbol, and Asset Icon + Favorite Star
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1.4f)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.nameFa,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.symbol,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Asset Icon Badge
                AssetIconBadge(item = item)

                // Favorite Toggle Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("fav_button_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "نشان کردن",
                        tint = if (item.isFavorite) FintechGold else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AssetIconBadge(item: ExchangeItem, modifier: Modifier = Modifier) {
    val (backgroundBrush, symbolText) = when (item.id) {
        "USD" -> Brush.linearGradient(listOf(FintechGreen, FintechCyan)) to "$"
        "USDT" -> Brush.linearGradient(listOf(Color(0xFF26A17B), Color(0xFF10B981))) to "₮"
        "EUR" -> Brush.linearGradient(listOf(Color(0xFF003399), FintechCyan)) to "€"
        "AED" -> Brush.linearGradient(listOf(FintechIndigo, FintechGold)) to "د.إ"
        "GBP" -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFC084FC))) to "£"
        "CAD" -> Brush.linearGradient(listOf(Color(0xFFEA580C), Color(0xFFF97316))) to "C$"
        "TRY" -> Brush.linearGradient(listOf(FintechRed, Color(0xFFFB7185))) to "₺"
        "USD_NIMA" -> Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF14B8A6))) to "نیما"
        "BTC" -> Brush.linearGradient(listOf(Color(0xFFF7931A), FintechGold)) to "₿"
        "ETH" -> Brush.linearGradient(listOf(Color(0xFF627EEA), FintechCyan)) to "Ξ"
        "SOL" -> Brush.linearGradient(listOf(Color(0xFF9945FF), Color(0xFF14F195))) to "◎"
        "BNB" -> Brush.linearGradient(listOf(Color(0xFFF3BA2F), Color(0xFFE5A118))) to "BNB"
        "XRP" -> Brush.linearGradient(listOf(Color(0xFF23292F), Color(0xFF00AAE4))) to "✕"
        "TON" -> Brush.linearGradient(listOf(Color(0xFF0088CC), Color(0xFF55ACEE))) to "💎"
        "DOGE" -> Brush.linearGradient(listOf(Color(0xFFC2A633), Color(0xFFE1B846))) to "Ð"
        "ADA" -> Brush.linearGradient(listOf(Color(0xFF0033AD), Color(0xFF3366FF))) to "₳"
        "TRX" -> Brush.linearGradient(listOf(Color(0xFFEF0027), Color(0xFFFF5252))) to "TRX"
        "AVAX" -> Brush.linearGradient(listOf(Color(0xFFE84142), Color(0xFFFF7273))) to "▲"
        "SHIB" -> Brush.linearGradient(listOf(Color(0xFFFFA409), Color(0xFFFF6400))) to "🐕"
        "GOLD_18K", "GOLD_24K" -> Brush.linearGradient(listOf(FintechGold, Color(0xFFFDE047))) to "⚜"
        "SEKKE_EMAMI", "SEKKE_BAHAR", "SEKKE_NIM", "SEKKE_ROB", "SEKKE_GERMI" ->
            Brush.linearGradient(listOf(Color(0xFFD97706), FintechGold)) to "🪙"
        "OUNCE_GOLD" -> Brush.linearGradient(listOf(FintechGold, Color(0xFFB45309))) to "OZ"
        else -> Brush.linearGradient(listOf(SurfaceCardLight, SurfaceCardBorder)) to item.symbol.take(2)
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbolText,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (symbolText.length > 2) 10.sp else 15.sp
        )
    }
}
