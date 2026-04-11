package com.example.staybuddy.domain.model

data class AutocompletePrediction(
    val placeId: Long,
    val primaryText: String,
    val secondaryText: String,
    val lat: Double,
    val lon: Double
)
