package com.example.service

import android.util.Log
import com.example.data.local.PriceAlertEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SyncState(val labelFa: String, val icon: String) {
    IDLE("آماده", "⚪"),
    SYNCING("در حال همگام‌سازی ابری...", "🔄"),
    SYNCED("همگام با Firestore", "☁️"),
    OFFLINE("آفلاین محلی", "💾")
}

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private var firestore: FirebaseFirestore? = null
    private var watchlistListener: ListenerRegistration? = null
    private var alertsListener: ListenerRegistration? = null

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime.asStateFlow()

    fun init() {
        try {
            firestore = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization note", e)
        }
    }

    fun syncFavoriteToCloud(userId: String, assetId: String, isFavorite: Boolean) {
        if (userId.isBlank()) return
        _syncState.value = SyncState.SYNCING
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId)
                .collection("watchlist").document(assetId)

            if (isFavorite) {
                val data = mapOf(
                    "assetId" to assetId,
                    "isFavorite" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
                docRef.set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncState.value = SyncState.SYNCED
                        _lastSyncTime.value = "لحظاتی پیش"
                        Log.d(TAG, "Watchlist item $assetId saved to Firestore")
                    }
                    .addOnFailureListener {
                        _syncState.value = SyncState.OFFLINE
                    }
            } else {
                docRef.delete()
                    .addOnSuccessListener {
                        _syncState.value = SyncState.SYNCED
                    }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync error", e)
            _syncState.value = SyncState.OFFLINE
        }
    }

    fun syncAlertToCloud(userId: String, alert: PriceAlertEntity) {
        if (userId.isBlank()) return
        _syncState.value = SyncState.SYNCING
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId)
                .collection("alerts").document(alert.id)

            val data = mapOf(
                "id" to alert.id,
                "assetId" to alert.assetId,
                "assetSymbol" to alert.assetSymbol,
                "assetNameFa" to alert.assetNameFa,
                "targetPriceToman" to alert.targetPriceToman,
                "condition" to alert.condition.name,
                "percentThreshold" to alert.percentThreshold,
                "isEnabled" to alert.isEnabled,
                "fcmTopic" to alert.fcmTopic,
                "updatedAt" to System.currentTimeMillis()
            )

            docRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    _syncState.value = SyncState.SYNCED
                    _lastSyncTime.value = "لحظاتی پیش"
                    Log.d(TAG, "Alert ${alert.id} saved to Firestore")
                }
                .addOnFailureListener {
                    _syncState.value = SyncState.OFFLINE
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore alert sync error", e)
            _syncState.value = SyncState.OFFLINE
        }
    }

    fun deleteAlertFromCloud(userId: String, alertId: String) {
        if (userId.isBlank()) return
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("alerts").document(alertId)
                .delete()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore alert delete error", e)
        }
    }

    fun listenToCloudData(
        userId: String,
        onWatchlistUpdated: (List<String>) -> Unit = {}
    ) {
        if (userId.isBlank()) return
        try {
            watchlistListener?.remove()
            val db = firestore ?: FirebaseFirestore.getInstance()
            watchlistListener = db.collection("users").document(userId)
                .collection("watchlist")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Watchlist snapshot error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val ids = snapshot.documents.mapNotNull { it.getString("assetId") }
                        if (ids.isNotEmpty()) {
                            onWatchlistUpdated(ids)
                            _syncState.value = SyncState.SYNCED
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "listenToCloudData error", e)
        }
    }

    fun stopListening() {
        watchlistListener?.remove()
        watchlistListener = null
        alertsListener?.remove()
        alertsListener = null
        _syncState.value = SyncState.IDLE
    }
}
