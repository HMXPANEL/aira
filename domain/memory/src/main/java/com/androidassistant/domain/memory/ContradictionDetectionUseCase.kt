package com.androidassistant.domain.memory

import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.agent.llm.LLMRequest
import com.androidassistant.core.common.Result
import com.androidassistant.core.model.Memory
import com.androidassistant.core.model.Message
import com.androidassistant.core.model.MessageRole
import java.util.UUID

class ContradictionDetectionUseCase(
    private val llmProvider: LLMProvider
) {

    data class Contradiction(
        val existingFact: Memory,
        val newFact: Memory,
        val reason: String,
        val resolution: Resolution
    )

    enum class Resolution {
        KEEP_EXISTING,
        REPLACE_WITH_NEW,
        KEEP_BOTH,
        MERGE
    }

    suspend fun detectContradictions(
        newFact: Memory,
        existingFacts: List<Memory>
    ): Result<List<Contradiction>> {
        if (existingFacts.isEmpty()) return Result.success(emptyList())

        val prompt = """
            Compare the new fact against existing facts and identify any contradictions.
            Return JSON array of contradictions, each with:
            - "existing_fact_id": ID of the existing fact
            - "reason": why they contradict
            - "resolution": "KEEP_EXISTING" | "REPLACE_WITH_NEW" | "KEEP_BOTH" | "MERGE"

            New fact: ${newFact.content}
            Existing facts:
            ${existingFacts.mapIndexed { i, f -> "${i}. [${f.id}] ${f.content}" }.joinToString("\n")}
        """.trimIndent()

        return try {
            val now = System.currentTimeMillis()
            val request = LLMRequest(
                systemPrompt = "You are a contradiction detection engine. Output only valid JSON array.",
                messages = listOf(
                    Message(
                        id = UUID.randomUUID().toString(),
                        sessionId = "contradiction-check",
                        role = MessageRole.USER,
                        content = prompt,
                        timestamp = now
                    )
                )
            )

            val response = llmProvider.generate(request)
            response.map { response ->
                parseContradictions(response.text ?: "[]", existingFacts, newFact)
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private fun parseContradictions(
        json: String,
        existingFacts: List<Memory>,
        newFact: Memory
    ): List<Contradiction> {
        return try {
            val arr = org.json.JSONArray(json)
            arr.toList().mapNotNull { item ->
                try {
                    val obj = item as org.json.JSONObject
                    val existingId = obj.getString("existing_fact_id")
                    val existingFact = existingFacts.find { it.id == existingId } ?: return@mapNotNull null
                    Contradiction(
                        existingFact = existingFact,
                        newFact = newFact,
                        reason = obj.getString("reason"),
                        resolution = Resolution.valueOf(obj.getString("resolution"))
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}