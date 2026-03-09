package com.example.staybuddy.data.model

data class ChatRoom(
    val roomId: String = "",
    val participants: List<String> = emptyList(),
    val listingId: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L
)
