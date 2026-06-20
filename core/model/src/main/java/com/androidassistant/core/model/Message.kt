package com.androidassistant.core.model

data class Message(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val toolCalls: List<ToolCall> = emptyList(),
    val tokenCount: Int = 0
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}
