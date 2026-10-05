package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.local.AlertCondition
import com.example.data.local.PriceAlertEntity
import com.example.data.model.NotificationType
import com.example.data.model.SmartNotification
import com.example.util.Formatters
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"
    private val _fcmToken = MutableStateFlow<String?>("fcm_token_ready")
    val fcmToken: StateFlow<String?> = _fcmToken

    fun init(context: Context) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    _fcmToken.value = token
                    Log.d(TAG, "FCM Registration Token initialized: $token")
                } else {
                    Log.w(TAG, "Fetching FCM token failed: ${task.exception?.message}")
                }
            }
            // Auto subscribe to general market alerts topic
            FirebaseMessaging.getInstance().subscribeToTopic("market_alerts_all")
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseMessaging initialization note", e)
        }
    }

    fun setToken(token: String) {
        _fcmToken.value = token
    }

    fun subscribeToPriceAlert(alert: PriceAlertEntity) {
        try {
            val topic = alert.fcmTopic
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Subscribed to FCM topic: $topic")
                    }
                }
            // Also subscribe to asset-level topic
            val assetTopic = "price_alert_${alert.assetId.lowercase()}"
            FirebaseMessaging.getInstance().subscribeToTopic(assetTopic)
        } catch (e: Exception) {
            Log.w(TAG, "Topic subscription note", e)
        }
    }

    fun unsubscribeFromPriceAlert(alert: PriceAlertEntity) {
        try {
            val topic = alert.fcmTopic
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            val assetTopic = "price_alert_${alert.assetId.lowercase()}"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(assetTopic)
        } catch (e: Exception) {
            Log.w(TAG, "Topic unsubscription note", e)
        }
    }

    fun subscribeToAssetTopic(assetId: String) {
        try {
            val topic = "price_alert_${assetId.lowercase()}"
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        } catch (e: Exception) {
            Log.w(TAG, "Topic subscription note", e)
        }
    }

    fun unsubscribeFromAssetTopic(assetId: String) {
        try {
            val topic = "price_alert_${assetId.lowercase()}"
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        } catch (e: Exception) {
            Log.w(TAG, "Topic unsubscription note", e)
        }
    }

    fun triggerCustomPriceAlertPush(
        context: Context,
        alert: PriceAlertEntity,
        currentPriceToman: Long,
        currentPriceUsd: Double,
        isTest: Boolean = false,
        onNotificationCreated: (SmartNotification) -> Unit
    ) {
        val prefix = if (isTest) "🔔 [تست پوش FCM] " else "🎯 [هشدار قیمت] "
        val targetFormatted = Formatters.formatToman(alert.targetPriceToman)
        val currentFormatted = Formatters.formatToman(currentPriceToman)

        val (title, message) = when (alert.condition) {
            AlertCondition.ABOVE -> {
                val t = "$prefix صعود ${alert.assetNameFa} به بالای آستانه"
                val m = if (isTest) {
                    "پوش نوتیفیکیشن Firebase فعال است: اگر نرخ ${alert.assetNameFa} از $targetFormatted عبور کند، این هشدار دریافت می‌شود (قیمت فعلی: $currentFormatted)."
                } else {
                    "نرخ لحظه‌ای ${alert.assetNameFa} به $currentFormatted رسید و از آستانه هدف شما ($targetFormatted) صعود کرد! 🚀"
                }
                Pair(t, m)
            }
            AlertCondition.BELOW -> {
                val t = "$prefix نزول ${alert.assetNameFa} به زیر آستانه (فرصت خرید)"
                val m = if (isTest) {
                    "پوش نوتیفیکیشن Firebase فعال است: اگر نرخ ${alert.assetNameFa} به زیر $targetFormatted برسد، این هشدار دریافت می‌شود (قیمت فعلی: $currentFormatted)."
                } else {
                    "نرخ لحظه‌ای ${alert.assetNameFa} به $currentFormatted افت کرد و به زیر آستانه مشخص‌شده ($targetFormatted) رسید. 📉"
                }
                Pair(t, m)
            }
            AlertCondition.PERCENT_CHANGE -> {
                val t = "$prefix نوسان شدید در بازار ${alert.assetNameFa}"
                val m = if (isTest) {
                    "پوش نوتیفیکیشن Firebase فعال است: در صورت نوسان بیش از ${alert.percentThreshold}٪ در ۲۴ ساعت، هشدار دریافت خواهد شد."
                } else {
                    "نماد ${alert.assetNameFa} با نوسان شدید در ۲۴ ساعت مواجه شد و به آستانه موردنظر شما رسید. قیمت لحظه‌ای: $currentFormatted"
                }
                Pair(t, m)
            }
        }

        val smartNotif = SmartNotification(
            title = title,
            message = message,
            type = NotificationType.PRICE_ALERT
        )

        onNotificationCreated(smartNotif)

        val notifId = (alert.id + System.currentTimeMillis()).hashCode()
        NotificationHelper.showSystemNotification(
            context = context,
            notificationId = notifId,
            title = title,
            message = message
        )
    }

    fun triggerPriceAlertNotification(
        context: Context,
        assetNameFa: String,
        targetPriceToman: Long,
        currentPriceToman: Long,
        onNotificationCreated: (SmartNotification) -> Unit
    ) {
        val title = "🎯 هدف قیمتی محقق شد: $assetNameFa"
        val message = "نرخ $assetNameFa به ${Formatters.formatToman(currentPriceToman)} رسید و از هدف تعیین‌شده (${Formatters.formatToman(targetPriceToman)}) عبور کرد!"

        val notification = SmartNotification(
            title = title,
            message = message,
            type = NotificationType.PRICE_ALERT
        )

        onNotificationCreated(notification)
        NotificationHelper.showSystemNotification(
            context = context,
            notificationId = assetNameFa.hashCode(),
            title = title,
            message = message
        )
    }
}
