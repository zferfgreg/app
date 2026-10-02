package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val id: String,
    val isFavorite: Boolean = true,
    val alertTargetPriceToman: Long? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
)
