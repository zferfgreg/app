package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.ui.components.AssetDetailSheet
import com.example.ui.components.ExchangeItemCard
import com.example.ui.components.MarketHeaderCard
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MarketUiState
import com.example.ui.viewmodel.SortOrder
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    uiState: MarketUiState,
    onSelectCategory: (AssetType) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onRefresh: () -> Unit,
    onItemClick: (ExchangeItem) -> Unit,
    onToggleFavorite: (ExchangeItem) -> Unit,
    onCloseDetail: () -> Unit,
    onSetAlert: (ExchangeItem, Long?) -> Unit,
    onOpenConverter: (ExchangeItem) -> Unit,
    unreadNotificationsCount: Int = 0,
    onOpenNotifications: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Market Overview Header Card
            item {
                MarketHeaderCard(
                    items = uiState.items,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    unreadNotificationsCount = unreadNotificationsCount,
                    onOpenNotifications = onOpenNotifications
                )
            }

            // Live Market Pulse Strip (نبض زنده بازار)
            if (uiState.items.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ نبض لحظه‌ای بازار",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FintechGold
                            )
                            Text(
                                text = "برای مشاهده نمودار ضربه بزنید",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val topMovers = remember(uiState.items) {
                            val movers = mutableListOf<ExchangeItem>()
                            uiState.items.find { it.id == "USD" }?.let { movers.add(it) }
                            uiState.items.find { it.id == "GOLD_18K" }?.let { movers.add(it) }
                            uiState.items.find { it.id == "SEKKE_EMAMI" }?.let { movers.add(it) }
                            uiState.items.find { it.id == "BTC" }?.let { movers.add(it) }
                            uiState.items.find { it.id == "USDT" }?.let { movers.add(it) }
                            uiState.items.find { it.id == "ETH" }?.let { movers.add(it) }
                            movers
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            topMovers.forEach { mover ->
                                val isPos = mover.changePercent24h >= 0
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceCard)
                                        .border(
                                            1.dp,
                                            if (isPos) FintechGreen.copy(alpha = 0.35f) else FintechRed.copy(alpha = 0.35f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { onItemClick(mover) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = mover.nameFa,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp
                                                ),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = Formatters.formatToman(mover.priceToman),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = TextSecondary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isPos) FintechGreenBg else FintechRedBg)
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = Formatters.formatPercent(mover.changePercent24h),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = if (isPos) FintechGreen else FintechRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Sort Dropdown Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sort Button
                    Box {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceCard)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                                .clickable { sortMenuExpanded = true }
                                .testTag("sort_filter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "فیلتر و مرتب‌سازی",
                                tint = if (uiState.sortOrder != SortOrder.DEFAULT) FintechCyan else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier.background(SurfaceCard)
                        ) {
                            SortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.titleFa,
                                            color = if (uiState.sortOrder == order) FintechCyan else TextPrimary,
                                            fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortOrderChange(order)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Search Field
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("market_search_input"),
                        placeholder = {
                            Text(
                                text = "جستجو (دلار، تتر، بیت‌کوین، سکه، طلا...)",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        leadingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = TextMuted)
                                }
                            }
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "جستجو", tint = TextMuted)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard,
                            focusedBorderColor = FintechCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            }

            // Categories Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        AssetType.ALL,
                        AssetType.DOLLAR_FIAT,
                        AssetType.CRYPTO,
                        AssetType.GOLD,
                        AssetType.WATCHLIST
                    )

                    categories.forEach { cat ->
                        val isSelected = uiState.selectedCategory == cat
                        val chipBg = if (isSelected) FintechCyan.copy(alpha = 0.15f) else SurfaceCard
                        val chipBorder = if (isSelected) FintechCyan else SurfaceCardBorder
                        val chipTextColor = if (isSelected) FintechCyan else TextSecondary

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(12.dp))
                                .clickable { onSelectCategory(cat) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("cat_chip_${cat.name}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cat == AssetType.WATCHLIST) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isSelected) FintechGold else TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = cat.titleFa,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp
                                    ),
                                    color = chipTextColor
                                )
                            }
                        }
                    }
                }
            }

            // Current Active Sort / Filter Tag (if applicable)
            if (uiState.sortOrder != SortOrder.DEFAULT) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لغو فیلتر",
                            color = FintechCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSortOrderChange(SortOrder.DEFAULT) }
                        )
                        Text(
                            text = "مرتب‌سازی بر اساس: ${uiState.sortOrder.titleFa}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Empty State
            if (uiState.filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (uiState.selectedCategory == AssetType.WATCHLIST) {
                                    "هنوز ارزی به دیده‌بان اضافه نکرده‌اید!\nبا زدن روی ستاره ⭐ در کنار هر ارز، آن را به دیده‌بان خود اضافه کنید."
                                } else {
                                    "موردی با این مشخصات یافت نشد."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Exchange Items List
            items(
                items = uiState.filteredItems,
                key = { it.id },
                contentType = { "exchange_item" }
            ) { item ->
                ExchangeItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onToggleFavorite = { onToggleFavorite(item) }
                )
            }
        }

        // Selected Asset Detail BottomSheet
        if (uiState.selectedDetailItem != null) {
            AssetDetailSheet(
                item = uiState.selectedDetailItem,
                sheetState = sheetState,
                onDismiss = onCloseDetail,
                onToggleFavorite = onToggleFavorite,
                onSetAlert = onSetAlert,
                onOpenConverter = onOpenConverter
            )
        }
    }
}
