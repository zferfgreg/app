package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.ExchangeItem
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatchedAssetsHeatmap(
    items: List<ExchangeItem>,
    onAssetClick: (ExchangeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOnlyWatched by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ExchangeItem?>(null) }

    val watchedItems = remember(items) { items.filter { it.isFavorite || it.alertPriceToman != null } }
    val displayItems = remember(items, showOnlyWatched, watchedItems) {
        if (showOnlyWatched && watchedItems.isNotEmpty()) watchedItems
        else items.take(18)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SurfaceCardBorder, RoundedCornerShape(20.dp))
            .testTag("watched_assets_heatmap_card"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Heatmap title & Watched Filter Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Switcher: All vs Watched
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardLight)
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!showOnlyWatched) FintechCyan else Color.Transparent)
                            .clickable { showOnlyWatched = false }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "کل بازار",
                            fontSize = 11.sp,
                            fontWeight = if (!showOnlyWatched) FontWeight.Bold else FontWeight.Medium,
                            color = if (!showOnlyWatched) Color.Black else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (showOnlyWatched) FintechGold else Color.Transparent)
                            .clickable { showOnlyWatched = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (showOnlyWatched) Color.Black else FintechGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "دیده‌بان من (${watchedItems.size})",
                                fontSize = 11.sp,
                                fontWeight = if (showOnlyWatched) FontWeight.Bold else FontWeight.Medium,
                                color = if (showOnlyWatched) Color.Black else TextSecondary
                            )
                        }
                    }
                }

                // Title & Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "نقشه حرارتی بازدهی (Heatmap)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "عملکرد ۲۴ ساعت گذشته دارایی‌ها",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
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
                            imageVector = Icons.Default.GridView,
                            contentDescription = null,
                            tint = FintechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D3/Recharts Color-Coded Heatmap Grid
            if (displayItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ دارایی در دیده‌بان انتخاب نشده است. دارایی‌ها را نشانه‌گذاری کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    maxItemsInEachRow = 3
                ) {
                    displayItems.forEach { item ->
                        val isSelected = selectedItem?.id == item.id
                        HeatmapCell(
                            item = item,
                            isSelected = isSelected,
                            onSelect = {
                                selectedItem = if (isSelected) null else item
                            },
                            onDoubleClick = {
                                onAssetClick(item)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Selected Asset Bar
            AnimatedVisibility(
                visible = selectedItem != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedItem?.let { sel ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCardLight)
                            .border(1.dp, FintechCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { onAssetClick(sel) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مشاهده تحلیل و نمودار کامل",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechCyan
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = FintechCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Formatters.formatToman(sel.priceToman),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sel.nameFa,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // D3 Color Legend Scale
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "-۳٪ (افت شدید)", fontSize = 9.5.sp, color = FintechRed)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF991B1B),
                                    Color(0xFFE11D48),
                                    Color(0xFF334155),
                                    Color(0xFF16A34A),
                                    Color(0xFF059669)
                                )
                            )
                        )
                )
                Text(text = "+۳٪ (رشد قوی)", fontSize = 9.5.sp, color = FintechGreen)
            }
        }
    }
}

@Composable
private fun HeatmapCell(
    item: ExchangeItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val change = item.changePercent24h
    val isPositive = change >= 0

    // Precise D3 Heatmap Color Grading based on 24h change
    val cellColor = when {
        change >= 2.5 -> Color(0xFF059669)     // Deep Emerald Green
        change >= 0.8 -> Color(0xFF16A34A)     // Medium Vibrant Green
        change > 0.0 -> Color(0xFF22C55E).copy(alpha = 0.85f) // Soft Green
        change == 0.0 -> Color(0xFF334155)    // Neutral Slate
        change >= -0.8 -> Color(0xFFF43F5E).copy(alpha = 0.85f) // Soft Coral Red
        change >= -2.5 -> Color(0xFFE11D48)    // Vivid Crimson
        else -> Color(0xFF991B1B)             // Deep Dark Red
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) FintechGold else Color.White.copy(alpha = 0.12f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cell_border"
    )

    Box(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cellColor)
            .border(if (isSelected) 2.dp else 1.dp, animatedBorderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("heatmap_cell_${item.id}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Symbol & Favorite indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (item.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = FintechGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = item.symbol,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Persian Name (Abbreviated)
            Text(
                text = item.nameFa,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Change badge with arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${if (isPositive) "+" else ""}$change%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
