package com.example.staybuddy.data.model

import com.google.firebase.firestore.PropertyName

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
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,
    val rating: Float = 0f,
    val ownerName: String = "",
    val ownerProfileImage: String = "",
    val ownerPhone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Trust & verification hooks
    val isVerified: Boolean = false,
    val reportCount: Int = 0,
    // Monetization hooks (future use)
    val isPremium: Boolean = false,
    val boostExpiresAt: Long? = null,
    val featuredUntil: Long? = null,
    val viewCount: Int = 0
) {
    constructor() : this("")
}
