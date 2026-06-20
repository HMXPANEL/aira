package com.androidassistant.domain.memory

import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.agent.llm.LLMRequest
import com.androidassistant.core.common.Result
import com.androidassistant.core.model.Message
import com.androidassistant.core.model.MessageRole
import java.util.UUID

class FactExtractionUseCase(
    private val llmProvider: LLMProvider
) {

    data class ExtractedFact(
        val content: String,
        val category: String,
        val confidence: Float,
        val entities: List<String>
    )

    data class ExtractionResult(
        val facts: List<ExtractedFact>,
        val summary: String
    )

    suspend fun extractFromConversation(
        messages: List<Message>,
        sessionId: String
    ): Result<ExtractionResult> {
        val conversationText = messages
            .filter { it.role != MessageRole.TOOL }
            .map { "${it.role.name}: ${it.content}" }
            .joinToString("\n")

        val prompt = """
            Analyze this conversation and extract key facts about the user.
            Return a JSON object with:
            - "facts": array of facts, each with "content", "category" (preference/fact/pattern), "confidence" (0-1), "entities"
            - "summary": one-sentence summary of the conversation

            Conversation:
            $conversationText
        """.trimIndent()

        return try {
            val now = System.currentTimeMillis()
            val request = LLMRequest(
                systemPrompt = "You are a fact extraction engine. Output only valid JSON.",
                messages = listOf(
                    Message(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.USER,
                        content = prompt,
                        timestamp = now
                    )
                )
            )

            val response = llmProvider.generate(request)
            response.map { response ->
                parseExtractionResult(response.text ?: "{\"facts\":[],\"summary\":\"\"}")
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private fun parseExtractionResult(json: String): ExtractionResult {
        return try {
            val obj = org.json.JSONObject(json)
            val facts = obj.getJSONArray("facts").toList()
                .mapNotNull { item ->
                    try {
                        val f = item as org.json.JSONObject
                        ExtractedFact(
                            content = f.getString("content"),
                            category = f.getString("category"),
                            confidence = f.getDouble("confidence").toFloat(),
                            entities = f.getJSONArray("entities").toList().map { it.toString() }
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            val summary = obj.optString("summary", "")
            ExtractionResult(facts = facts, summary = summary)
        } catch (e: Exception) {
            ExtractionResult(emptyList(), "")
        }
    }
}