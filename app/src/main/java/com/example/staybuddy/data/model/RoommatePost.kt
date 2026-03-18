package com.example.staybuddy.data.model

enum class RoommatePostType {
    OFFER, SEEK
}

data class RoommatePost(
    val postId: String = "",
    val userId: String = "",
    val city: String = "",
    val location: String = "",
    val priceShare: Int = 0,
    val availableBeds: Int = 0,
    val totalBeds: Int = 0,
    val roomType: String = "", // Shared, Single, etc.
    val postType: RoommatePostType = RoommatePostType.OFFER,
    val description: String = "",
    val preferences: Map<String, String> = emptyMap(),
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val userName: String = "",
    val userProfileImage: String = "",
    @get:com.google.firebase.firestore.PropertyName("isActive")
    @set:com.google.firebase.firestore.PropertyName("isActive")
    var isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")
}
