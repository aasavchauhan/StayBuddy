package com.example.staybuddy.data.model

data class PgListing(
    val listingId: String = "",
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val city: String = "",
    val area: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val price: Int = 0,
    val deposit: Int = 0,
    val roomType: String = "",
    val genderAllowed: String = "",
    val amenities: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val availableBeds: Int = 0,
    val isActive: Boolean = true,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)
