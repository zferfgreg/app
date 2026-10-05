package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExchangeItem
import com.example.data.model.PriceSource
import com.example.data.model.SourceQuote
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechRed
import com.example.util.Formatters

@Composable
fun PriceSourceSelectorCard(
    item: ExchangeItem,
    onSelectSource: (PriceSource) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val sources = item.sources

    if (sources.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(20.dp))
            .testTag("price_source_selector_card"),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FintechGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "منبع فعال: ${item.selectedSource.shortName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechGold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "انتخاب منبع قیمت و آربیتراژ",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "مقایسه نرخ‌های زنده صرافی‌ها و سایت‌ها",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FintechCyan.copy(alpha = 0.15f))
                            .border(1.dp, FintechCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "برای تغییر مبنای قیمت این دارایی در سراسر برنامه، روی هر منبع ضربه بزنید:",
                fontSize = 11.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // List of Sources
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sources.forEach { quote ->
                    val isSelected = quote.source == item.selectedSource
                    SourceQuoteRow(
                        quote = quote,
                        isSelected = isSelected,
                        basePriceToman = item.priceToman,
                        onSelect = { onSelectSource(quote.source) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceQuoteRow(
    quote: SourceQuote,
    isSelected: Boolean,
    basePriceToman: Long,
    onSelect: () -> Unit
) {
    val colors = AppTheme.colors
    val diffToman = quote.priceToman - basePriceToman
    val diffPercent = if (basePriceToman > 0) (diffToman.toDouble() / basePriceToman.toDouble()) * 100.0 else 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) FintechCyan.copy(alpha = 0.1f) else colors.surfaceLight)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) FintechCyan else colors.surfaceBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
            .testTag("source_row_${quote.source.name}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Price & Difference
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Formatters.formatToman(quote.priceToman),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                if (!isSelected && diffToman != 0L) {
                    val sign = if (diffToman > 0) "+" else ""
                    Text(
                        text = "$sign${Formatters.formatToman(diffToman)} ($sign${String.format("%.2f", diffPercent)}%)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (diffToman > 0) FintechGreen else FintechRed
                    )
                } else if (isSelected) {
                    Text(
                        text = "✓ منبع فعال کنونی",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechCyan
                    )
                }
            }

            // Right: Source Info & Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = FintechCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = quote.source.nameFa,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (quote.volumeLabel.isNotBlank()) {
                            Text(
                                text = quote.volumeLabel,
                                fontSize = 9.5.sp,
                                color = colors.textMuted
                            )
                            Text(text = " • ", fontSize = 9.sp, color = colors.textMuted)
                        }
                        Text(
                            text = quote.source.website,
                            fontSize = 9.5.sp,
                            color = FintechCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) FintechCyan.copy(alpha = 0.2f) else colors.surface)
                        .border(1.dp, if (isSelected) FintechCyan else colors.surfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = quote.source.icon, fontSize = 16.sp)
                }
            }
        }
    }
}
