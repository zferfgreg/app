package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechGreenBg
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechRedBg
import com.example.ui.viewmodel.SortOrder
import com.example.util.Formatters

@Composable
fun PersistentSearchHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    allItems: List<ExchangeItem>,
    selectedCategory: AssetType,
    onSelectCategory: (AssetType) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    onItemClick: (ExchangeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var isSearchFocused by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Auto-complete filtered suggestions
    val suggestions by remember(searchQuery, allItems) {
        derivedStateOf {
            if (searchQuery.trim().length >= 1) {
                val q = searchQuery.trim().lowercase()
                allItems.filter { item ->
                    item.nameFa.lowercase().contains(q) ||
                    item.nameEn.lowercase().contains(q) ||
                    item.symbol.lowercase().contains(q) ||
                    item.id.lowercase().contains(q)
                }.take(6)
            } else {
                emptyList()
            }
        }
    }

    val trendingTags = listOf(
        "دلار" to "USD",
        "طلا ۱۸" to "GOLD_18K",
        "سکه امامی" to "SEKKE_EMAMI",
        "تتر" to "USDT",
        "بیت‌کوین" to "BTC",
        "درهم" to "AED",
        "یورو" to "EUR"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
        color = colors.surfaceLight,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Search Bar & Sort Dropdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort Menu Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.surfaceBorder, RoundedCornerShape(13.dp))
                            .clickable { sortMenuExpanded = true }
                            .testTag("persistent_sort_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "مرتب‌سازی",
                            tint = if (sortOrder != SortOrder.DEFAULT) FintechCyan else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        SortOrder.values().forEach { order ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = order.titleFa,
                                        color = if (sortOrder == order) FintechCyan else colors.textPrimary,
                                        fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.5.sp
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

                // Real-time Persistent Search Input with Auto-complete
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isSearchFocused = it.isFocused }
                        .testTag("persistent_search_input"),
                    placeholder = {
                        Text(
                            text = "جستجوی زنده (دلار، طلا، بیت‌کوین، تتر...)",
                            color = colors.textMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    leadingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "پاک کردن",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = if (searchQuery.isNotEmpty()) FintechCyan else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(13.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        focusedBorderColor = FintechCyan,
                        unfocusedBorderColor = colors.surfaceBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )
            }

            // Real-Time Auto-Complete Suggestion List (Popup Dropdown)
            AnimatedVisibility(
                visible = suggestions.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, FintechCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(8.dp)
                        .testTag("autocomplete_dropdown")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${suggestions.size} نتیجه یافت شد",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "پیشنهادات تکمیل خودکار",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FintechCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = FintechCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    suggestions.forEach { item ->
                        val isPos = item.changePercent24h >= 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSearchQueryChange(item.nameFa)
                                    onItemClick(item)
                                }
                                .padding(horizontal = 8.dp, vertical = 7.dp)
                                .testTag("autocomplete_item_${item.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Price & Change
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isPos) FintechGreenBg else FintechRedBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = Formatters.formatPercent(item.changePercent24h),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isPos) FintechGreen else FintechRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Formatters.formatToman(item.priceToman),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }

                            // Asset Details
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = item.nameFa,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = item.symbol,
                                        fontSize = 10.sp,
                                        color = colors.textMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                AssetIconBadge(item = item, size = 30.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Trending Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trendingTags.forEach { (label, symbol) ->
                    val isSelected = searchQuery.equals(label, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) FintechGold.copy(alpha = 0.2f) else colors.surface)
                            .border(
                                1.dp,
                                if (isSelected) FintechGold else colors.surfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isSelected) {
                                    onSearchQueryChange("")
                                } else {
                                    onSearchQueryChange(label)
                                }
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) FintechGold else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Categories Filter Chips Row (همه، ارزها، کریپتو، طلا و سکه، دیده‌بان)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val categories = listOf(
                    AssetType.ALL,
                    AssetType.DOLLAR_FIAT,
                    AssetType.CRYPTO,
                    AssetType.GOLD,
                    AssetType.WATCHLIST
                )

                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val chipBg = if (isSelected) FintechCyan.copy(alpha = 0.16f) else colors.surface
                    val chipBorder = if (isSelected) FintechCyan else colors.surfaceBorder
                    val chipTextColor = if (isSelected) FintechCyan else colors.textSecondary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectCategory(cat) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("persistent_cat_chip_${cat.name}")
                    ) {
                        Text(
                            text = cat.titleFa,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = chipTextColor
                        )
                    }
                }
            }
        }
    }
}
