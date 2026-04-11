package com.example.staybuddy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.staybuddy.domain.model.AutocompletePrediction

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val placeId: Long,
    val primaryText: String,
    val secondaryText: String,
    val lat: Double,
    val lon: Double,
    val timestamp: Long = System.currentTimeMillis()
)

fun SearchHistoryEntity.toDomainModel() = AutocompletePrediction(
    placeId = placeId,
    primaryText = primaryText,
    secondaryText = secondaryText,
    lat = lat,
    lon = lon
)

fun AutocompletePrediction.toEntity() = SearchHistoryEntity(
    placeId = placeId,
    primaryText = primaryText,
    secondaryText = secondaryText,
    lat = lat,
    lon = lon
)
