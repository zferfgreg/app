package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AlertCondition(val titleFa: String, val icon: String, val description: String) {
    ABOVE("صعود به بالای قیمت", "📈", "هشدار هنگامی که قیمت از این آستانه عبور کرده و بیشتر شود"),
    BELOW("نزول به پایین قیمت", "📉", "هشدار هنگامی که قیمت از این آستانه کمتر شود (فرصت خرید یا کف قیمتی)"),
    PERCENT_CHANGE("نوسان شدید ۲۴ ساعته", "⚡", "هشدار در صورت تغییرات سریع بیش از درصد مشخص")
}

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val assetId: String,
    val assetSymbol: String,
    val assetNameFa: String,
    val targetPriceToman: Long,
    val targetPriceUsd: Double = 0.0,
    val condition: AlertCondition = AlertCondition.ABOVE,
    val percentThreshold: Double = 5.0,
    val isEnabled: Boolean = true,
    val fcmTopic: String = "price_alert_${assetId.lowercase()}",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val triggeredCount: Int = 0,
    val lastTriggeredTime: String? = null
)
