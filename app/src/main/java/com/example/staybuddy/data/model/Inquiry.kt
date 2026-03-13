package com.example.staybuddy.data.model

data class Inquiry(
    val inquiryId: String = "",
    val listingId: String = "",
    val userId: String = "", // The person inquiring
    val hostId: String = "", // The owner of the listing
    val moveInDate: Long = 0,
    val roomType: String = "",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")
}
