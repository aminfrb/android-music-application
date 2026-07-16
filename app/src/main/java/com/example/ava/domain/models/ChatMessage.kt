package com.example.ava.domain.models

enum class MessageStatus { SENDING, SENT, DELIVERED, READ }
enum class MessageType { TEXT, SONG }

data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String? = null,
    val song: Song? = null,
    val timestamp: Long,
    val status: MessageStatus,
    val type: MessageType
)
