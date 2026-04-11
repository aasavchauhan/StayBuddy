package com.example.staybuddy.data.model

data class Report(
    val reportId: String = "",
    val listingId: String = "",
    val reporterId: String = "",
    val reason: String = "",
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
