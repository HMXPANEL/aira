package com.androidassistant.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidassistant.agent.engine.AgentOrchestrator
import com.androidassistant.agent.engine.safety.ApprovalCallback
import com.androidassistant.agent.engine.safety.DefaultApprovalCallback
import com.androidassistant.core.common.Result
import com.androidassistant.core.model.AgentMode
import com.androidassistant.core.model.ApprovalRequest
import com.androidassistant.core.model.ApprovalResult
import com.androidassistant.core.model.DeviceContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val agentOrchestrator: AgentOrchestrator,
    private val approvalCallback: DefaultApprovalCallback
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(sessionId = UUID.randomUUID().toString()) }
        observeAgentState()
        observeApprovals()
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isProcessing) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            isUser = true,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update {
            it.copy(
                inputText = "",
                messages = it.messages + userMessage,
                isProcessing = true,
                agentMode = AgentMode.PROCESSING,
                error = null
            )
        }

        viewModelScope.launch {
            val result = agentOrchestrator.processInput(
                userMessage = text,
                sessionId = _uiState.value.sessionId,
                deviceContext = _uiState.value.deviceContext
            )

            when (result) {
                is Result.Success -> {
                    val assistantMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = result.data,
                        isUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + assistantMessage,
                            isProcessing = false,
                            agentMode = AgentMode.IDLE
                        )
                    }
                }
                is Result.Error -> {
                    val errorMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        content = result.message ?: result.exception.message ?: "Unknown error",
                        isUser = false,
                        timestamp = System.currentTimeMillis(),
                        isError = true
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMessage,
                            isProcessing = false,
                            agentMode = AgentMode.ERROR,
                            error = result.message ?: result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun updateDeviceContext(context: DeviceContext) {
        _uiState.update { it.copy(deviceContext = context) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearConversation() {
        _uiState.update {
            it.copy(
                messages = emptyList(),
                sessionId = UUID.randomUUID().toString(),
                error = null
            )
        }
        agentOrchestrator.reset()
    }

    fun respondToApproval(approve: Boolean, modifiedArgs: Map<String, Any>? = null) {
        val request = _uiState.value.pendingApproval
        if (request != null) {
            val result = if (approve) {
                if (modifiedArgs != null) {
                    ApprovalResult.ApprovedWithModification(modifiedArgs)
                } else {
                    ApprovalResult.Approved()
                }
            } else {
                ApprovalResult.Denied("User denied")
            }
            approvalCallback.respond(request.id, result)
            _uiState.update { it.copy(pendingApproval = null) }
        }
    }

    private fun observeAgentState() {
        viewModelScope.launch {
            agentOrchestrator.state.collect { state ->
                _uiState.update {
                    it.copy(
                        isProcessing = state.isProcessing,
                        agentMode = state.mode,
                        error = state.lastError
                    )
                }
            }
        }
    }

    private fun observeApprovals() {
        viewModelScope.launch {
            val channel = approvalCallback.getApprovalChannel()
            for (request in channel) {
                _uiState.update { it.copy(pendingApproval = request) }
            }
        }
    }
}