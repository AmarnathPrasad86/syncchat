package com.amar.syncchat.data.model

data class Message(
    val id: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: Long = 0L,
    val isToxic: Boolean = false,
    val aiSuggestion: String? = null,
    val status: String? = "sent"
)
