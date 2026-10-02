package com.example.data.model

import java.util.UUID

enum class NotificationType {
    PRICE_ALERT,
    VOLATILITY,
    AI_INSIGHT,
    MARKET_STATUS
}

data class SmartNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetItemId: String? = null
)
