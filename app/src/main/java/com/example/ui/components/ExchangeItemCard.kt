package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechIndigo
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.util.Formatters

@Composable
fun ExchangeItemCard(
    item: ExchangeItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val isPositive = item.changePercent24h >= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("exchange_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.88f)),
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
                    color = colors.textPrimary,
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
                        color = colors.textMuted
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

            // Right Column: Name, Symbol, Source Badge & Asset Icon + Favorite Star
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
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${item.selectedSource.icon} ${item.selectedSource.shortName}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechGold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.symbol,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = colors.textSecondary,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                        tint = if (item.isFavorite) FintechGold else colors.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
