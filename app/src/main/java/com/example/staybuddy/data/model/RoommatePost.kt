package com.example.staybuddy.data.model

data class RoommatePost(
    val postId: String = "",
    val userId: String = "",
    val city: String = "",
    val location: String = "",
    val priceShare: Int = 0,
    val availableBeds: Int = 0,
    val preferences: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)
