package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AssetType
import com.example.data.model.ExchangeItem
import com.example.data.repository.ExchangeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val repository = ExchangeRepository(database.watchlistDao())

    private val _filterParams = MutableStateFlow(FilterParams())

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
            }
        }
    }

    fun toggleFavorite(item: ExchangeItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun setTargetAlert(item: ExchangeItem, targetToman: Long?) {
        viewModelScope.launch {
            repository.setTargetAlert(item.id, targetToman)
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
