package com.example.wardenchat.data

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long,
    val isMine: Boolean,
)

enum class ConnectionStatus {
    OFFLINE,
    READY,
    CONNECTED
}
