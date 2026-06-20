package com.androidassistant.data.remote.gemini

data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata? = null
)

data class Candidate(
    val content: ResponseContent,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

data class ResponseContent(
    val role: String?,
    val parts: List<ResponsePart>
)

data class ResponsePart(
    val text: String? = null,
    val functionCall: ResponseFunctionCall? = null
)

data class ResponseFunctionCall(
    val name: String,
    val args: Map<String, Any>?
)

data class SafetyRating(
    val category: String,
    val probability: String
)

data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)

data class GeminiStreamChunk(
    val text: String? = null,
    val functionCall: ResponseFunctionCall? = null,
    val isComplete: Boolean = false,
    val error: String? = null,
    val usageMetadata: UsageMetadata? = null
)

data class GeminiError(
    val error: GeminiErrorDetail
)

data class GeminiErrorDetail(
    val code: Int,
    val message: String,
    val status: String
)
