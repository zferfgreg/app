package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AlertCondition
import com.example.data.local.AppDatabase
import com.example.data.local.PriceAlertEntity
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.data.model.FinancialNewsItem
import com.example.data.model.MarketSentimentData
import com.example.data.model.NotificationType
import com.example.data.model.PriceSource
import com.example.data.model.SmartNotification
import com.example.data.remote.GeminiService
import com.example.data.repository.ExchangeRepository
import com.example.service.FcmTokenManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOrder(val titleFa: String) {
    DEFAULT("پیش‌فرض بازار"),
    GAINERS("بیشترین صعود (+٪)"),
    LOSERS("بیشترین نزول (-٪)"),
    PRICE_DESC("بیشترین قیمت (تومان)"),
    PRICE_ASC("کمترین قیمت")
}

enum class MessageSender {
    USER, AI
}

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FilterParams(
    val selectedCategory: AssetType = AssetType.ALL,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val isRefreshing: Boolean = false,
    val selectedDetailItem: ExchangeItem? = null,
    val useRials: Boolean = false,
    val errorMessage: String? = null
)

data class MarketUiState(
    val items: List<ExchangeItem> = emptyList(),
    val filteredItems: List<ExchangeItem> = emptyList(),
    val selectedCategory: AssetType = AssetType.ALL,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val isRefreshing: Boolean = false,
    val selectedDetailItem: ExchangeItem? = null,
    val useRials: Boolean = false,
    val errorMessage: String? = null
)

class ExchangeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ExchangeRepository(database.watchlistDao(), database.priceAlertDao())

    val allPriceAlerts: StateFlow<List<PriceAlertEntity>> = repository.allPriceAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterParams = MutableStateFlow(FilterParams())

    // Grounded News & Market Sentiment State
    val groundedNews = MutableStateFlow<List<FinancialNewsItem>>(emptyList())
    val marketSentiment = MutableStateFlow(MarketSentimentData())
    val isLoadingNews = MutableStateFlow(false)

    init {
        FcmTokenManager.init(application)
        loadInitialNewsAndSentiment()
    }

    private fun loadInitialNewsAndSentiment() {
        viewModelScope.launch {
            val news = GeminiService.fetchGroundedNews(listOf("USD", "GOLD_18K", "BTC"))
            groundedNews.value = news
            marketSentiment.value = GeminiService.calculateMarketSentiment(uiState.value.items, news)
        }
    }

    fun fetchGroundedNews() {
        viewModelScope.launch {
            isLoadingNews.value = true
            val watched = uiState.value.items.filter { it.isFavorite || it.alertPriceToman != null }.map { it.symbol }
            val news = GeminiService.fetchGroundedNews(watched)
            groundedNews.value = news
            marketSentiment.value = GeminiService.calculateMarketSentiment(uiState.value.items, news)
            isLoadingNews.value = false
        }
    }

    fun refreshMarketSentiment() {
        viewModelScope.launch {
            marketSentiment.value = GeminiService.calculateMarketSentiment(uiState.value.items, groundedNews.value)
        }
    }

    // Converter State
    val converterFromItem = MutableStateFlow<ExchangeItem?>(null)
    val converterToItem = MutableStateFlow<ExchangeItem?>(null)
    val converterAmount = MutableStateFlow("1")

    // AI Market Analyst State
    val aiChatMessages = MutableStateFlow<List<AiChatMessage>>(listOf(
        AiChatMessage(
            sender = MessageSender.AI,
            text = "سلام! من دستیار هوش مصنوعی تحلیلی بازار EXCHANCE هستم 🤖\nتمام نرخ‌های زنده دلار، طلا، سکه و کریپتو را به لحظه در اختیار دارم. چه تحلیلی مدنظر شماست؟"
        )
    ))
    val isAiAnalyzing = MutableStateFlow(false)

    fun askAi(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank() || isAiAnalyzing.value) return
        val userMsg = AiChatMessage(sender = MessageSender.USER, text = cleanPrompt)
        aiChatMessages.update { it + userMsg }
        isAiAnalyzing.value = true

        viewModelScope.launch {
            val currentItems = uiState.value.items
            val response = com.example.data.remote.GeminiService.analyzeMarket(cleanPrompt, currentItems)
            val aiMsg = AiChatMessage(sender = MessageSender.AI, text = response)
            aiChatMessages.update { it + aiMsg }
            isAiAnalyzing.value = false
        }
    }

    fun clearAiChat() {
        aiChatMessages.value = listOf(
            AiChatMessage(
                sender = MessageSender.AI,
                text = "سلام! من دستیار هوش مصنوعی تحلیلی بازار EXCHANCE هستم 🤖\nتمام نرخ‌های زنده دلار، طلا، سکه و کریپتو را به لحظه در اختیار دارم. چه تحلیلی مدنظر شماست؟"
            )
        )
    }

    // Smart Notifications State
    val smartNotifications = MutableStateFlow<List<SmartNotification>>(listOf(
        SmartNotification(
            title = "خوش‌آمدید به EXCHANCE 🚀",
            message = "نرخ‌های زنده بازار طلا، سکه، دلار و کریپتو با موفقیت بارگذاری شدند.",
            type = NotificationType.MARKET_STATUS,
            isRead = false
        ),
        SmartNotification(
            title = "سیگنال هوش مصنوعی روز 🤖",
            message = "دلار و تتر در محدوده تثبیت قرار دارند. طلای ۱۸ عیار همچنان سپر دفاعی ضد تورم است.",
            type = NotificationType.AI_INSIGHT,
            isRead = false
        )
    ))

    val unreadNotificationsCount: StateFlow<Int> = smartNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val isNotificationSheetOpen = MutableStateFlow(false)

    fun openNotificationSheet() {
        isNotificationSheetOpen.value = true
        markAllNotificationsRead()
    }

    fun closeNotificationSheet() {
        isNotificationSheetOpen.value = false
    }

    fun markAllNotificationsRead() {
        smartNotifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun clearNotifications() {
        smartNotifications.value = emptyList()
    }

    fun addNotification(notification: SmartNotification) {
        smartNotifications.update { listOf(notification) + it }
        try {
            NotificationHelper.showSystemNotification(
                getApplication(),
                notification.id.hashCode(),
                notification.title,
                notification.message
            )
        } catch (e: Exception) {
            // Ignored
        }
    }

    // Advanced Settings State
    val autoRefreshSec = MutableStateFlow(30)
    val hapticEnabled = MutableStateFlow(true)
    val highVolatilityAlert = MutableStateFlow(true)
    val aiPersona = MutableStateFlow("متعادل و منطقی")
    val defaultGoldWage = MutableStateFlow(7)

    // Theme Mode & Animated Background State
    val isDarkTheme = MutableStateFlow(true)
    val isAnimationEnabled = MutableStateFlow(true)
    val selectedSourceFilter = MutableStateFlow<PriceSource?>(null)

    fun toggleTheme() { isDarkTheme.value = !isDarkTheme.value }
    fun setDarkTheme(dark: Boolean) { isDarkTheme.value = dark }
    fun toggleAnimation() { isAnimationEnabled.value = !isAnimationEnabled.value }
    fun setAnimationEnabled(enabled: Boolean) { isAnimationEnabled.value = enabled }

    fun setAssetPriceSource(assetId: String, newSource: PriceSource) {
        repository.setAssetPriceSource(assetId, newSource)
    }

    fun setGlobalSourceFilter(source: PriceSource?) {
        selectedSourceFilter.value = source
        repository.setGlobalPriceSource(source)
    }

    fun setAutoRefreshSec(sec: Int) { autoRefreshSec.value = sec }
    fun toggleHaptic() { hapticEnabled.value = !hapticEnabled.value }
    fun toggleVolatilityAlert() { highVolatilityAlert.value = !highVolatilityAlert.value }
    fun setAiPersona(persona: String) { aiPersona.value = persona }
    fun setDefaultGoldWage(wage: Int) { defaultGoldWage.value = wage }
    fun clearCache() {
        viewModelScope.launch {
            refreshRates()
        }
    }

    val uiState: StateFlow<MarketUiState> = combine(
        repository.allItems,
        _filterParams
    ) { allItems, params ->
        var list = allItems

        // Filter by Category
        if (params.selectedCategory == AssetType.WATCHLIST) {
            list = list.filter { it.isFavorite || it.alertPriceToman != null }
        } else if (params.selectedCategory != AssetType.ALL) {
            list = list.filter { it.type == params.selectedCategory }
        }

        // Filter by Search Query
        if (params.searchQuery.isNotBlank()) {
            val q = params.searchQuery.trim().lowercase()
            list = list.filter {
                it.nameFa.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.symbol.lowercase().contains(q)
            }
        }

        // Sort items
        list = when (params.sortOrder) {
            SortOrder.DEFAULT -> list
            SortOrder.GAINERS -> list.sortedByDescending { it.changePercent24h }
            SortOrder.LOSERS -> list.sortedBy { it.changePercent24h }
            SortOrder.PRICE_DESC -> list.sortedByDescending { it.priceToman }
            SortOrder.PRICE_ASC -> list.sortedBy { it.priceToman }
        }

        // Keep detail item updated if present
        val currentDetail = if (params.selectedDetailItem != null) {
            allItems.find { it.id == params.selectedDetailItem.id } ?: params.selectedDetailItem
        } else null

        // Initialize converter items if not yet set
        if (converterFromItem.value == null && allItems.isNotEmpty()) {
            converterFromItem.value = allItems.find { it.id == "USD" } ?: allItems.first()
        }
        if (converterToItem.value == null && allItems.isNotEmpty()) {
            converterToItem.value = allItems.find { it.id == "USDT" } ?: allItems.getOrNull(1)
        }

        MarketUiState(
            items = allItems,
            filteredItems = list,
            selectedCategory = params.selectedCategory,
            searchQuery = params.searchQuery,
            sortOrder = params.sortOrder,
            isRefreshing = params.isRefreshing,
            selectedDetailItem = currentDetail,
            useRials = params.useRials,
            errorMessage = params.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MarketUiState()
    )

    init {
        refreshRates()
    }

    fun selectCategory(category: AssetType) {
        _filterParams.update { it.copy(selectedCategory = category) }
    }

    fun updateSearchQuery(query: String) {
        _filterParams.update { it.copy(searchQuery = query) }
    }

    fun setSortOrder(order: SortOrder) {
        _filterParams.update { it.copy(sortOrder = order) }
    }

    fun openDetail(item: ExchangeItem) {
        _filterParams.update { it.copy(selectedDetailItem = item) }
    }

    fun closeDetail() {
        _filterParams.update { it.copy(selectedDetailItem = null) }
    }

    fun toggleUseRials() {
        _filterParams.update { it.copy(useRials = !it.useRials) }
    }

    fun refreshRates() {
        viewModelScope.launch {
            _filterParams.update { it.copy(isRefreshing = true, errorMessage = null) }
            val result = repository.refreshRates()
            _filterParams.update { it.copy(isRefreshing = false) }
            if (result.isFailure) {
                _filterParams.update { it.copy(errorMessage = "عدم دسترسی به اینترنت، قیمت‌های آفلاین فعال است") }
            } else {
                val currentItems = uiState.value.items
                val timeString = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

                // 1. Evaluate custom Firebase Price Alerts
                val activeAlerts = allPriceAlerts.value.filter { it.isEnabled }
                activeAlerts.forEach { alert ->
                    val matchedItem = currentItems.find { it.id == alert.assetId }
                    if (matchedItem != null) {
                        val triggered = when (alert.condition) {
                            AlertCondition.ABOVE -> matchedItem.priceToman >= alert.targetPriceToman
                            AlertCondition.BELOW -> matchedItem.priceToman <= alert.targetPriceToman
                            AlertCondition.PERCENT_CHANGE -> kotlin.math.abs(matchedItem.changePercent24h) >= alert.percentThreshold
                        }
                        if (triggered) {
                            FcmTokenManager.triggerCustomPriceAlertPush(
                                context = getApplication(),
                                alert = alert,
                                currentPriceToman = matchedItem.priceToman,
                                currentPriceUsd = matchedItem.priceUsd,
                                isTest = false,
                                onNotificationCreated = { addNotification(it) }
                            )
                            repository.markAlertTriggered(alert.id, timeString)
                        }
                    }
                }

                // 2. Evaluate legacy quick alert price
                currentItems.forEach { item ->
                    val alert = item.alertPriceToman
                    if (alert != null && item.priceToman >= alert) {
                        FcmTokenManager.triggerPriceAlertNotification(
                            context = getApplication(),
                            assetNameFa = item.nameFa,
                            targetPriceToman = alert,
                            currentPriceToman = item.priceToman,
                            onNotificationCreated = { addNotification(it) }
                        )
                    }
                }
                marketSentiment.value = GeminiService.calculateMarketSentiment(currentItems, groundedNews.value)
            }
        }
    }

    fun saveCustomPriceAlert(alert: PriceAlertEntity) {
        viewModelScope.launch {
            repository.savePriceAlert(alert)
            addNotification(
                SmartNotification(
                    title = "🎯 ثبت هشدار سفارشی: ${alert.assetNameFa}",
                    message = "پوش نوتیفیکیشن Firebase برای آستانه ${com.example.util.Formatters.formatToman(alert.targetPriceToman)} با موفقیت فعال شد.",
                    type = NotificationType.PRICE_ALERT,
                    targetItemId = alert.assetId
                )
            )
        }
    }

    fun deletePriceAlert(id: String) {
        viewModelScope.launch {
            repository.deletePriceAlertById(id)
        }
    }

    fun togglePriceAlert(id: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.togglePriceAlert(id, isEnabled)
        }
    }

    fun testPriceAlertPush(alert: PriceAlertEntity) {
        val currentItem = uiState.value.items.find { it.id == alert.assetId }
        val currentToman = currentItem?.priceToman ?: alert.targetPriceToman
        val currentUsd = currentItem?.priceUsd ?: alert.targetPriceUsd
        FcmTokenManager.triggerCustomPriceAlertPush(
            context = getApplication(),
            alert = alert,
            currentPriceToman = currentToman,
            currentPriceUsd = currentUsd,
            isTest = true,
            onNotificationCreated = { addNotification(it) }
        )
    }

    fun toggleFavorite(item: ExchangeItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun setTargetAlert(item: ExchangeItem, targetToman: Long?) {
        viewModelScope.launch {
            repository.setTargetAlert(item.id, targetToman)
            if (targetToman != null) {
                FcmTokenManager.subscribeToAssetTopic(item.id)
                addNotification(
                    SmartNotification(
                        title = "🎯 ثبت آستانه هشدار قیمت: ${item.nameFa}",
                        message = "هدف قیمتی بر روی ${"%,d".format(targetToman)} تومان فعال شد. در صورت رسیدن یا عبور قیمت، بلافاصله مطلع می‌شوید.",
                        type = NotificationType.PRICE_ALERT,
                        targetItemId = item.id
                    )
                )
            } else {
                FcmTokenManager.unsubscribeFromAssetTopic(item.id)
            }
        }
    }

    fun setupConverterFor(item: ExchangeItem) {
        converterFromItem.value = item
        val all = uiState.value.items
        if (item.id == "USD") {
            converterToItem.value = all.find { it.id == "USDT" }
        } else {
            converterToItem.value = all.find { it.id == "USD" }
        }
    }

    fun swapConverterItems() {
        val temp = converterFromItem.value
        converterFromItem.value = converterToItem.value
        converterToItem.value = temp
    }

    fun setConverterFrom(item: ExchangeItem) {
        converterFromItem.value = item
    }

    fun setConverterTo(item: ExchangeItem) {
        converterToItem.value = item
    }

    fun updateConverterAmount(amount: String) {
        converterAmount.value = amount
    }
}
