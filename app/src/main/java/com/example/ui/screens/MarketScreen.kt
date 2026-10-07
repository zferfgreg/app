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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.local.PriceAlertEntity
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.data.model.FinancialNewsItem
import com.example.data.model.MarketSentimentData
import com.example.data.model.PriceSource
import com.example.service.SyncState
import com.example.service.UserProfile
import com.example.ui.components.AnimatedFintechBackground
import com.example.ui.components.AssetDetailSheet
import com.example.ui.components.CustomPriceAlertSheet
import com.example.ui.components.ExchangeItemCard
import com.example.ui.components.GroundedNewsSection
import com.example.ui.components.MarketHeaderCard
import com.example.ui.components.MarketSentimentSection
import com.example.ui.components.PersistentSearchHeader
import com.example.ui.components.PriceSourceFilterBar
import com.example.ui.components.ScrollDownBlurEffect
import com.example.ui.components.ScrollMotionBlurEffect
import com.example.ui.components.rememberScrollVelocity
import com.example.ui.components.scrollMotionBlur
import com.example.ui.components.WatchedAssetsHeatmap
import com.example.ui.components.WorldClocksCard
import com.example.ui.theme.AppTheme
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
    sentiment: MarketSentimentData = MarketSentimentData(),
    newsList: List<FinancialNewsItem> = emptyList(),
    isLoadingNews: Boolean = false,
    onRefreshSentiment: () -> Unit = {},
    onRefreshNews: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    isAnimationEnabled: Boolean = true,
    onToggleAnimation: () -> Unit = {},
    selectedSourceFilter: PriceSource? = null,
    onSelectSourceFilter: (PriceSource?) -> Unit = {},
    onSelectAssetSource: (String, PriceSource) -> Unit = { _, _ -> },
    customAlerts: List<PriceAlertEntity> = emptyList(),
    onSaveCustomAlert: (PriceAlertEntity) -> Unit = {},
    onDeleteCustomAlert: (String) -> Unit = {},
    onToggleCustomAlert: (String, Boolean) -> Unit = { _, _ -> },
    onTestCustomAlert: (PriceAlertEntity) -> Unit = {},
    currentUser: UserProfile? = null,
    isAuthLoading: Boolean = false,
    syncState: SyncState = SyncState.IDLE,
    onSignInGoogle: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onPinWidget: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val listState = rememberLazyListState()
    val motionVelocity by rememberScrollVelocity(listState)
    val scrollOffset by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex * 300f + listState.firstVisibleItemScrollOffset
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var itemForCustomAlert by remember { mutableStateOf<ExchangeItem?>(null) }

    AnimatedFintechBackground(
        isDarkTheme = isDarkTheme,
        isAnimationEnabled = isAnimationEnabled,
        scrollOffset = scrollOffset,
        scrollVelocity = motionVelocity,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
        // 1. Persistent Search Header with Real-Time Auto-Complete
        PersistentSearchHeader(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            allItems = uiState.items,
            selectedCategory = uiState.selectedCategory,
            onSelectCategory = onSelectCategory,
            sortOrder = uiState.sortOrder,
            onSortOrderChange = onSortOrderChange,
            onItemClick = onItemClick
        )

        // 2. Scrollable Market Content with Dynamic Motion Blur
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .scrollMotionBlur(motionVelocity),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            // Market Overview Header Card
            item {
                MarketHeaderCard(
                    items = uiState.items,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = onRefresh,
                    onPinWidget = onPinWidget,
                    unreadNotificationsCount = unreadNotificationsCount,
                    onOpenNotifications = onOpenNotifications,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    isAnimationEnabled = isAnimationEnabled,
                    onToggleAnimation = onToggleAnimation
                )
            }

            // Price Source Selector Chip Bar ("از سایت های که قیمت میگیری بیشتر کن بزار انتخاب کرد تو ارز")
            item {
                PriceSourceFilterBar(
                    selectedSource = selectedSourceFilter,
                    onSelectSource = onSelectSourceFilter
                )
            }

            // Market Sentiment Meter Section
            item {
                MarketSentimentSection(
                    sentiment = sentiment,
                    onRefreshSentiment = onRefreshSentiment
                )
            }

            // Live World Clocks (London, Tehran, Vancouver & Global Hubs)
            item {
                WorldClocksCard()
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

            // D3/Recharts Color-Coded Heatmap Data Visualization Component
            item {
                Spacer(modifier = Modifier.height(4.dp))
                WatchedAssetsHeatmap(
                    items = uiState.items,
                    onAssetClick = onItemClick
                )
            }

            // Google Search Grounded Financial News Section
            if (newsList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    GroundedNewsSection(
                        newsList = newsList,
                        isLoading = isLoadingNews,
                        onRefreshNews = onRefreshNews
                    )
                }
            }
        }

        // Dynamic Motion Blur Effect (Replaced scroll down button)
        ScrollMotionBlurEffect(
            listState = listState,
            bottomPadding = 20.dp
        )
    }
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
            onOpenConverter = onOpenConverter,
            onSelectSource = { source ->
                onSelectAssetSource(uiState.selectedDetailItem.id, source)
            },
            customAlerts = customAlerts,
            onOpenCustomAlert = {
                itemForCustomAlert = uiState.selectedDetailItem
            },
            onDeleteCustomAlert = onDeleteCustomAlert,
            onToggleCustomAlert = onToggleCustomAlert
        )
    }

    // Custom Firebase Price Alert Sheet (FCM Push Configuration)
    if (itemForCustomAlert != null) {
        CustomPriceAlertSheet(
            item = itemForCustomAlert!!,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismiss = { itemForCustomAlert = null },
            onSaveAlert = { alert ->
                onSaveCustomAlert(alert)
                itemForCustomAlert = null
            },
            onTestNotification = { alert ->
                onTestCustomAlert(alert)
            }
        )
    }
}
