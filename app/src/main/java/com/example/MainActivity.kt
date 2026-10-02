package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.ExchangeViewModel

enum class MainTab(val titleFa: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector, val tag: String) {
    MARKET("بازار", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, "nav_market"),
    CONVERTER("مبدل هوشمند", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz, "nav_converter"),
    WATCHLIST("دیده‌بان", Icons.Filled.Star, Icons.Outlined.StarBorder, "nav_watchlist"),
    SETTINGS("تنظیمات", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

class MainActivity : ComponentActivity() {
    private val viewModel: ExchangeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: ExchangeViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.MARKET) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fromItem by viewModel.converterFromItem.collectAsStateWithLifecycle()
    val toItem by viewModel.converterToItem.collectAsStateWithLifecycle()
    val converterAmount by viewModel.converterAmount.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.titleFa
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleFa,
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (tab == MainTab.WATCHLIST) FintechGold else FintechCyan,
                            selectedTextColor = TextPrimary,
                            indicatorColor = SurfaceCardLight,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.MARKET -> {
                    MarketScreen(
                        uiState = uiState,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onSortOrderChange = { viewModel.setSortOrder(it) },
                        onRefresh = { viewModel.refreshRates() },
                        onItemClick = { viewModel.openDetail(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onCloseDetail = { viewModel.closeDetail() },
                        onSetAlert = { item, alert -> viewModel.setTargetAlert(item, alert) },
                        onOpenConverter = {
                            viewModel.setupConverterFor(it)
                            currentTab = MainTab.CONVERTER
                        }
                    )
                }

                MainTab.CONVERTER -> {
                    ConverterScreen(
                        items = uiState.items,
                        fromItem = fromItem,
                        toItem = toItem,
                        amountString = converterAmount,
                        onAmountChange = { viewModel.updateConverterAmount(it) },
                        onSwap = { viewModel.swapConverterItems() },
                        onSelectFrom = { viewModel.setConverterFrom(it) },
                        onSelectTo = { viewModel.setConverterTo(it) }
                    )
                }

                MainTab.WATCHLIST -> {
                    WatchlistScreen(
                        items = uiState.items,
                        onItemClick = { viewModel.openDetail(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onNavigateToMarket = { currentTab = MainTab.MARKET }
                    )
                }

                MainTab.SETTINGS -> {
                    SettingsScreen(
                        useRials = uiState.useRials,
                        onToggleUseRials = { viewModel.toggleUseRials() }
                    )
                }
            }
        }
    }
}
