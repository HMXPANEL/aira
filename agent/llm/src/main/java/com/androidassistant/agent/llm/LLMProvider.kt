package com.androidassistant.agent.llm

import com.androidassistant.core.common.Result
import com.androidassistant.core.model.Message
import kotlinx.coroutines.flow.Flow

interface LLMProvider {

    suspend fun generate(
        request: LLMRequest
    ): Result<LLMResponse>

    fun stream(
        request: LLMRequest
    ): Flow<LLMStreamEvent>
}

data class LLMRequest(
    val systemPrompt: String,
    val messages: List<Message>,
    val tools: List<FunctionDeclaration> = emptyList(),
    val config: LLMConfig = LLMConfig()
)

data class LLMConfig(
    val model: String = "gemini-2.0-flash",
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 4096,
    val topP: Float = 0.95f
)

data class LLMResponse(
    val text: String?,
    val toolCalls: List<LLMFunctionCall> = emptyList(),
    val usage: LLMUsage? = null
)

data class LLMFunctionCall(
    val name: String,
    val args: Map<String, Any>
)

data class LLMUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)

sealed class LLMStreamEvent {
    data class Text(val content: String) : LLMStreamEvent()
    data class FunctionCall(val name: String, val args: Map<String, Any>) : LLMStreamEvent()
    data class Usage(val usage: LLMUsage) : LLMStreamEvent()
    data class Error(val message: String) : LLMStreamEvent()
    data object Complete : LLMStreamEvent()
}

data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: FunctionParameter
)

data class FunctionParameter(
    val type: String = "object",
    val properties: Map<String, ParameterProperty> = emptyMap(),
    val required: List<String> = emptyList()
)

data class ParameterProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)
