package com.androidassistant.agent.engine

import com.androidassistant.agent.engine.context.ContextAssembler
import com.androidassistant.agent.engine.safety.SafetyGate
import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.agent.llm.LLMRequest
import com.androidassistant.core.common.Constants
import com.androidassistant.core.common.Result
import com.androidassistant.core.model.AgentState
import com.androidassistant.core.model.DeviceContext
import com.androidassistant.core.model.Message
import com.androidassistant.core.model.MessageRole
import com.androidassistant.core.model.ToolCall
import com.androidassistant.core.model.ToolCallStatus
import com.androidassistant.core.model.ToolResult
import com.androidassistant.tool.registry.ToolRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgentOrchestrator(
    private val llmProvider: LLMProvider,
    private val contextAssembler: ContextAssembler,
    private val toolRegistry: ToolRegistry,
    private val safetyGate: SafetyGate
) {

    private val _state = MutableStateFlow(AgentState())
    val state: StateFlow<AgentState> = _state.asStateFlow()

    suspend fun processInput(
        userMessage: String,
        sessionId: String,
        deviceContext: DeviceContext
    ): Result<String> {
        _state.value = _state.value.copy(isProcessing = true, mode = com.androidassistant.core.model.AgentMode.PROCESSING)

        try {
            val messages = mutableListOf(
                Message(
                    id = "msg_${System.currentTimeMillis()}",
                    sessionId = sessionId,
                    role = MessageRole.USER,
                    content = userMessage,
                    timestamp = System.currentTimeMillis()
                )
            )

            val systemPrompt = contextAssembler.buildSystemPrompt(deviceContext, sessionId)
            val toolDeclarations = toolRegistry.getFunctionDeclarations()

            var iteration = 0
            while (iteration < Constants.MAX_AGENT_ITERATIONS) {
                _state.value = _state.value.copy(iterationCount = iteration)

                val request = LLMRequest(
                    systemPrompt = systemPrompt,
                    messages = messages,
                    tools = toolDeclarations
                )

                when (val result = llmProvider.generate(request)) {
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            lastError = result.message ?: result.exception.message,
                            mode = com.androidassistant.core.model.AgentMode.ERROR
                        )
                        return Result.error(result.exception)
                    }
                    is Result.Success -> {
                        val response = result.data

                        response.text?.let { text ->
                            messages.add(
                                Message(
                                    id = "msg_${System.currentTimeMillis()}",
                                    sessionId = sessionId,
                                    role = MessageRole.ASSISTANT,
                                    content = text,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }

                        if (response.toolCalls.isEmpty()) {
                            val finalText = response.text ?: ""
                            _state.value = _state.value.copy(
                                isProcessing = false,
                                mode = com.androidassistant.core.model.AgentMode.IDLE
                            )
                            return Result.success(finalText)
                        }

                        for (toolCall in response.toolCalls) {
                            val toolDefinition = toolRegistry.resolve(toolCall.name)
                            if (toolDefinition == null) {
                                messages.add(buildToolErrorMessage(sessionId, toolCall.name, "Tool not found"))
                                continue
                            }

                            val safetyResult = safetyGate.evaluate(toolDefinition, toolCall.args)
                            if (!safetyResult.allowed) {
                                messages.add(buildToolErrorMessage(sessionId, toolCall.name, safetyResult.reason ?: "Blocked by safety"))
                                continue
                            }

                            val call = ToolCall(
                                id = "tc_${System.currentTimeMillis()}",
                                name = toolCall.name,
                                args = toolCall.args,
                                status = ToolCallStatus.RUNNING,
                                startedAt = System.currentTimeMillis()
                            )
                            _state.value = _state.value.copy(currentToolCall = call)

                            val toolResult = executeToolSafely(toolDefinition, toolCall.args)

                            val updatedCall = call.copy(
                                status = if (toolResult.success) ToolCallStatus.SUCCESS else ToolCallStatus.ERROR,
                                result = toolResult,
                                completedAt = System.currentTimeMillis()
                            )
                            _state.value = _state.value.copy(currentToolCall = updatedCall)

                            messages.add(
                                Message(
                                    id = "msg_${System.currentTimeMillis()}",
                                    sessionId = sessionId,
                                    role = MessageRole.TOOL,
                                    content = toolResult.data ?: toolResult.error ?: "No result",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }

                        iteration++
                    }
                }
            }

            _state.value = _state.value.copy(
                isProcessing = false,
                mode = com.androidassistant.core.model.AgentMode.IDLE
            )
            return Result.success("I've completed the task. Is there anything else you'd like me to do?")
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isProcessing = false,
                lastError = e.message,
                mode = com.androidassistant.core.model.AgentMode.ERROR
            )
            return Result.error(e)
        }
    }

    private suspend fun executeToolSafely(
        definition: com.androidassistant.tool.registry.ToolDefinition,
        args: Map<String, Any>
    ): ToolResult {
        return try {
            val result = kotlinx.coroutines.withTimeout(Constants.MAX_TOOL_TIMEOUT_MS) {
                definition.executor(args)
            }
            result
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            ToolResult(success = false, error = "Tool execution timed out")
        } catch (e: Exception) {
            ToolResult(success = false, error = e.message ?: "Unknown error")
        }
    }

    private fun buildToolErrorMessage(sessionId: String, toolName: String, reason: String): Message {
        return Message(
            id = "msg_${System.currentTimeMillis()}",
            sessionId = sessionId,
            role = MessageRole.TOOL,
            content = "{\"error\": \"$toolName failed: $reason\"}",
            timestamp = System.currentTimeMillis()
        )
    }

    fun reset() {
        _state.value = AgentState()
    }
}
