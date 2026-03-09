package com.example.staybuddy.data.model

data class ChatRoom(
    val roomId: String = "",
    val participants: List<String> = emptyList(),
    val listingId: String = "",
    val roommatePostId: String = "", // Link to roommate post if applicable
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val confirmedBy: List<String> = emptyList(),
    val isMatchConfirmed: Boolean = false
)
