package com.androidassistant.data.remote.gemini

data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: SystemInstruction? = null,
    val tools: List<Tool>? = null,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val role: String,
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val functionCall: FunctionCallPart? = null,
    val functionResponse: FunctionResponsePart? = null
)

data class FunctionCallPart(
    val name: String,
    val args: Map<String, Any>?
)

data class FunctionResponsePart(
    val name: String,
    val response: Map<String, Any>
)

data class SystemInstruction(
    val parts: List<Part>
)

data class Tool(
    val functionDeclarations: List<FunctionDeclaration>
)

data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Schema
)

data class Schema(
    val type: String,
    val properties: Map<String, Property>? = null,
    val required: List<String>? = null
)

data class Property(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 4096,
    val topP: Float = 0.95f,
    val topK: Int = 40
)
