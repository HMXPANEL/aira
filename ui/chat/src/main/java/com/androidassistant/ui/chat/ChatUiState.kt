package com.androidassistant.ui.chat

import com.androidassistant.core.model.AgentMode
import com.androidassistant.core.model.DeviceContext

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val agentMode: AgentMode = AgentMode.IDLE,
    val error: String? = null,
    val deviceContext: DeviceContext = DeviceContext(),
    val sessionId: String = ""
)

data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isStreaming: Boolean = false,
    val toolCallName: String? = null,
    val toolCallResult: String? = null,
    val isError: Boolean = false
)
