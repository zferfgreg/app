package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NotificationType
import com.example.data.model.SmartNotification
import com.example.ui.components.SmartNotificationSheet
import com.example.ui.screens.AiAnalystScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.MarketScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechPurple
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.ExchangeViewModel

enum class MainTab(val titleFa: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector, val tag: String) {
    MARKET("بازار", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp, "nav_market"),
    CONVERTER("مبدل", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz, "nav_converter"),
    AI_ANALYST("هوش مصنوعی", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai_analyst"),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ExchangeViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.MARKET) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fromItem by viewModel.converterFromItem.collectAsStateWithLifecycle()
    val toItem by viewModel.converterToItem.collectAsStateWithLifecycle()
    val converterAmount by viewModel.converterAmount.collectAsStateWithLifecycle()

    val smartNotifications by viewModel.smartNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val isNotificationSheetOpen by viewModel.isNotificationSheetOpen.collectAsStateWithLifecycle()

    val groundedNews by viewModel.groundedNews.collectAsStateWithLifecycle()
    val marketSentiment by viewModel.marketSentiment.collectAsStateWithLifecycle()
    val isLoadingNews by viewModel.isLoadingNews.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Request Android 13+ runtime POST_NOTIFICATIONS permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.dp, SurfaceCardBorder, RoundedCornerShape(26.dp))
                        .testTag("main_bottom_nav"),
                    color = SurfaceCard.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    shadowElevation = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MainTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            val activeColor = when (tab) {
                                MainTab.AI_ANALYST -> FintechPurple
                                MainTab.WATCHLIST -> FintechGold
                                else -> FintechCyan
                            }

                            val animatedScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.08f else 1.0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "tab_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .scale(animatedScale)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent
                                    )
                                    .clickable { currentTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag(tab.tag),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tab.titleFa,
                                        tint = if (isSelected) activeColor else TextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = tab.titleFa,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            fontSize = 10.5.sp
                                        ),
                                        color = if (isSelected) TextPrimary else TextMuted
                                    )
                                }
                            }
                        }
                    }
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
                        },
                        unreadNotificationsCount = unreadNotificationsCount,
                        onOpenNotifications = { viewModel.openNotificationSheet() },
                        sentiment = marketSentiment,
                        newsList = groundedNews,
                        isLoadingNews = isLoadingNews,
                        onRefreshSentiment = { viewModel.refreshMarketSentiment() },
                        onRefreshNews = { viewModel.fetchGroundedNews() }
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

                MainTab.AI_ANALYST -> {
                    AiAnalystScreen(
                        viewModel = viewModel
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
                        viewModel = viewModel
                    )
                }
            }
        }

        // Smart Notification Sheet
        if (isNotificationSheetOpen) {
            val notifSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            SmartNotificationSheet(
                notifications = smartNotifications,
                sheetState = notifSheetState,
                onDismiss = { viewModel.closeNotificationSheet() },
                onClearAll = { viewModel.clearNotifications() },
                onSimulateAlert = {
                    val randPrice = (93000..96000).random()
                    viewModel.addNotification(
                        SmartNotification(
                            title = "🎯 نوسان شدید قیمت: بیت‌کوین و تتر",
                            message = "نرخ تتر در بازار به $randPrice تومان رسید (نوسان روزانه فراتر از حد مجاز).",
                            type = NotificationType.VOLATILITY
                        )
                    )
                }
            )
        }
    }
}
