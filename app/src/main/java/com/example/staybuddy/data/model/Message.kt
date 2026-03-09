package com.example.staybuddy.data.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val seen: Boolean = false
)
