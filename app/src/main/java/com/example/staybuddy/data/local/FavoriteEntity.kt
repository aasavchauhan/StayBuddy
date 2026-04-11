package com.example.staybuddy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val listingId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
