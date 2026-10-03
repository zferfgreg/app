package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.model.NotificationType
import com.example.data.model.SmartNotification
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object FcmTokenManager {
    private const val TAG = "FcmTokenManager"
    private val _fcmToken = MutableStateFlow<String?>("fcm_token_initialized")
    val fcmToken: StateFlow<String?> = _fcmToken

    fun init(context: Context) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    _fcmToken.value = token
                    Log.d(TAG, "FCM Registration Token: $token")
                } else {
                    Log.w(TAG, "Fetching FCM token failed", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseMessaging initialization note", e)
        }
    }

    fun setToken(token: String) {
        _fcmToken.value = token
    }

    fun subscribeToAssetTopic(assetId: String) {
        try {
            val topic = "price_alert_${assetId.lowercase()}"
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Subscribed to FCM topic: $topic")
                    }
                }
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

    fun triggerPriceAlertNotification(
        context: Context,
        assetNameFa: String,
        targetPriceToman: Long,
        currentPriceToman: Long,
        onNotificationCreated: (SmartNotification) -> Unit
    ) {
        val title = "🎯 هدف قیمتی محقق شد: $assetNameFa"
        val message = "نرخ $assetNameFa به ${"%,d".format(currentPriceToman)} تومان رسید و از هدف تعیین‌شده (${"%,d".format(targetPriceToman)} تومان) عبور کرد!"

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
