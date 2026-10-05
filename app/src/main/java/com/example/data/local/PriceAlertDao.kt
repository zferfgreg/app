package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY createdTimestamp DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE assetId = :assetId ORDER BY createdTimestamp DESC")
    fun getAlertsForAsset(assetId: String): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE isEnabled = 1")
    suspend fun getActiveAlertsSync(): List<PriceAlertEntity>

    @Query("SELECT * FROM price_alerts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PriceAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlert(id: String)

    @Query("UPDATE price_alerts SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAlertEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE price_alerts SET triggeredCount = triggeredCount + 1, lastTriggeredTime = :time WHERE id = :id")
    suspend fun markAlertTriggered(id: String, time: String)
}
