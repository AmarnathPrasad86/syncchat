package com.amar.syncchat.data.model

data class Message(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long,
    val isToxic: Boolean = false,
    val aiSuggestion: String = "",
    val status: String = "sent"
)
