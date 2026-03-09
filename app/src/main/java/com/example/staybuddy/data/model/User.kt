package com.example.staybuddy.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "", // "student" or "owner"
    val gender: String = "",
    val city: String = "",
    val college: String = "",
    val profileImage: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
