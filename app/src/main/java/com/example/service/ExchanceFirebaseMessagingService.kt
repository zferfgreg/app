package com.example.service

import android.util.Log
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ExchanceFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        FcmTokenManager.setToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title
            ?: data["title"]
            ?: "🎯 هشدار قیمت EXCHANCE"

        val body = notification?.body
            ?: data["message"]
            ?: data["body"]
            ?: "ارز مورد نظر شما به آستانه قیمتی تعیین‌شده رسید."

        val assetId = data["asset_id"] ?: "GENERAL"
        val notificationId = (assetId + System.currentTimeMillis()).hashCode()

        NotificationHelper.showSystemNotification(
            context = applicationContext,
            notificationId = notificationId,
            title = title,
            message = body
        )
    }

    companion object {
        private const val TAG = "ExchanceFCMService"
    }
}
