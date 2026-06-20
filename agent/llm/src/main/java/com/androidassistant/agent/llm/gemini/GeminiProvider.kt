package com.androidassistant.agent.llm.gemini

import com.androidassistant.agent.llm.FunctionDeclaration
import com.androidassistant.agent.llm.LLMConfig
import com.androidassistant.agent.llm.LLMFunctionCall
import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.agent.llm.LLMRequest
import com.androidassistant.agent.llm.LLMResponse
import com.androidassistant.agent.llm.LLMStreamEvent
import com.androidassistant.agent.llm.LLMUsage
import com.androidassistant.agent.llm.ParameterProperty
import com.androidassistant.core.common.Result
import com.androidassistant.data.remote.gemini.GeminiApi
import com.androidassistant.data.remote.gemini.GeminiRequest
import com.androidassistant.data.remote.gemini.Part
import com.androidassistant.data.remote.gemini.Property
import com.androidassistant.data.remote.gemini.Schema
import com.androidassistant.data.remote.gemini.Content as GeminiContent
import com.androidassistant.data.remote.gemini.GenerationConfig as GeminiGenerationConfig
import com.androidassistant.data.remote.gemini.Tool as GeminiTool
import com.androidassistant.data.remote.gemini.FunctionDeclaration as GeminiFunctionDeclaration
import com.androidassistant.data.remote.gemini.SystemInstruction as GeminiSystemInstruction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GeminiProvider(private val api: GeminiApi) : LLMProvider {

    override suspend fun generate(request: LLMRequest): Result<LLMResponse> {
        val geminiRequest = buildGeminiRequest(request)
        val response = api.generateContent(geminiRequest, request.config.model)

        return response.map { geminiResponse ->
            val candidate = geminiResponse.candidates.firstOrNull()
            val text = candidate?.content?.parts?.firstOrNull { it.text != null }?.text
            val functionCalls = candidate?.content?.parts
                ?.filter { it.functionCall != null }
                ?.map { LLMFunctionCall(it.functionCall!!.name, it.functionCall!!.args ?: emptyMap()) }
                ?: emptyList()
            val usage = geminiResponse.usageMetadata?.let {
                LLMUsage(it.promptTokenCount, it.candidatesTokenCount, it.totalTokenCount)
            }

            LLMResponse(text = text, toolCalls = functionCalls, usage = usage)
        }
    }

    override fun stream(request: LLMRequest): Flow<LLMStreamEvent> = callbackFlow {
        val geminiRequest = buildGeminiRequest(request)

        val eventSource = api.streamGenerateContent(
            request = geminiRequest,
            model = request.config.model,
            onChunk = { chunk ->
                if (chunk.text != null) {
                    trySend(LLMStreamEvent.Text(chunk.text))
                }
                if (chunk.functionCall != null) {
                    trySend(LLMStreamEvent.FunctionCall(
                        name = chunk.functionCall.name,
                        args = chunk.functionCall.args ?: emptyMap()
                    ))
                }
                if (chunk.isComplete) {
                    chunk.usageMetadata?.let {
                        trySend(LLMStreamEvent.Usage(
                            LLMUsage(it.promptTokenCount, it.candidatesTokenCount, it.totalTokenCount)
                        ))
                    }
                    trySend(LLMStreamEvent.Complete)
                }
            },
            onComplete = {
                channel.close()
            },
            onError = { error ->
                trySend(LLMStreamEvent.Error(error))
                channel.close()
            }
        )

        awaitClose { eventSource.cancel() }
    }

    private fun buildGeminiRequest(request: LLMRequest): GeminiRequest {
        val contents = request.messages.map { message ->
            GeminiContent(
                role = message.role.name.lowercase(),
                parts = listOf(Part(text = message.content))
            )
        }

        val systemInstruction = GeminiSystemInstruction(
            parts = listOf(Part(text = request.systemPrompt))
        )

        val geminiTools = if (request.tools.isNotEmpty()) {
            listOf(GeminiTool(
                functionDeclarations = request.tools.map { mapFunction(it) }
            ))
        } else null

        val config = GeminiGenerationConfig(
            temperature = request.config.temperature,
            maxOutputTokens = request.config.maxOutputTokens,
            topP = request.config.topP
        )

        return GeminiRequest(
            contents = contents,
            systemInstruction = systemInstruction,
            tools = geminiTools,
            generationConfig = config
        )
    }

    private fun mapFunction(fn: FunctionDeclaration): GeminiFunctionDeclaration {
        return GeminiFunctionDeclaration(
            name = fn.name,
            description = fn.description,
            parameters = Schema(
                type = fn.parameters.type,
                properties = fn.parameters.properties.mapValues { (_, prop) ->
                    Property(type = prop.type, description = prop.description, enum = prop.enum)
                }.ifEmpty { null },
                required = fn.parameters.required.ifEmpty { null }
            )
        )
    }
}
