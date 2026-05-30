package com.amar.syncchat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amar.syncchat.data.model.Message

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String?,
    val receiverId: String?,
    val message: String?,
    val timestamp: Long,
    val isToxic: Boolean,
    val aiSuggestion: String?,
    val status: String?
)

fun MessageEntity.toMessage() = Message(
    id = id,
    senderId = senderId,
    receiverId = receiverId,
    message = message,
    timestamp = timestamp,
    isToxic = isToxic,
    aiSuggestion = aiSuggestion,
    status = status
)

fun Message.toEntity() = MessageEntity(
    id = id ?: "",
    senderId = senderId,
    receiverId = receiverId,
    message = message,
    timestamp = timestamp,
    isToxic = isToxic,
    aiSuggestion = aiSuggestion,
    status = status
)
