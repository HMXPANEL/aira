package com.androidassistant.ui.chat

import com.androidassistant.core.model.AgentMode
import com.androidassistant.core.model.ApprovalRequest
import com.androidassistant.core.model.DeviceContext

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val agentMode: AgentMode = AgentMode.IDLE,
    val error: String? = null,
    val sessionId: String = "",
    val deviceContext: DeviceContext = DeviceContext(),
    val pendingApproval: ApprovalRequest? = null
)

data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isError: Boolean = false,
    val toolCallName: String? = null
)