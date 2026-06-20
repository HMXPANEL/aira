package com.androidassistant.agent.memory

import androidx.sqlite.db.SimpleSQLiteQuery
import com.androidassistant.core.common.Result
import com.androidassistant.core.model.Memory
import com.androidassistant.core.model.MemoryType
import com.androidassistant.data.memory.dao.EpisodicMemoryDao
import com.androidassistant.data.memory.dao.SemanticMemoryDao
import com.androidassistant.data.memory.embedding.EmbeddingService
import com.androidassistant.data.memory.entity.EpisodicMemoryEntity
import com.androidassistant.data.memory.entity.SemanticMemoryEntity
import com.androidassistant.domain.memory.ContradictionDetectionUseCase
import com.androidassistant.domain.memory.FactExtractionUseCase
import java.util.UUID

class MemoryManager(
    private val semanticMemoryDao: SemanticMemoryDao,
    private val episodicMemoryDao: EpisodicMemoryDao,
    private val embeddingService: EmbeddingService,
    private val factExtraction: FactExtractionUseCase? = null,
    private val contradictionDetection: ContradictionDetectionUseCase? = null
) {

    suspend fun storeEpisodic(
        sessionId: String,
        summary: String,
        importance: Int,
        entities: List<String>? = null,
        toolCalls: String? = null
    ) {
        val memory = EpisodicMemoryEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            summary = summary,
            timestamp = System.currentTimeMillis(),
            importance = importance.coerceIn(1, 10),
            entitiesJson = entities?.let { org.json.JSONArray(it).toString() },
            toolCallsJson = toolCalls
        )
        episodicMemoryDao.insert(memory)
    }

    suspend fun storeSemantic(
        content: String,
        category: String = "fact",
        source: String = "inferred",
        importance: Int = 5,
        metadata: Map<String, String>? = null
    ): Result<String> {
        val embeddingResult = embeddingService.embed(content)
        return when (embeddingResult) {
            is Result.Error -> Result.error(embeddingResult.exception)
            is Result.Success -> {
                val memoryId = UUID.randomUUID().toString()
                val memory = SemanticMemoryEntity(
                    id = memoryId,
                    content = content,
                    embedding = embeddingService.floatArrayToByteArray(embeddingResult.data),
                    category = category,
                    source = source,
                    importance = importance.coerceIn(1, 10),
                    createdAt = System.currentTimeMillis(),
                    metadataJson = metadata?.let {
                        org.json.JSONObject(it).toString()
                    }
                )
                semanticMemoryDao.insert(memory)
                try {
                    semanticMemoryDao.insertVector(memoryId, memory.embedding)
                } catch (e: Exception) {
                }
                Result.success(memory.id)
            }
        }
    }

    suspend fun querySimilarMemories(
        query: String,
        limit: Int = 5,
        minSimilarity: Float = 0.65f
    ): Result<List<Memory>> {
        val queryEmbedding = embeddingService.embed(query)
        return when (queryEmbedding) {
            is Result.Error -> Result.error(queryEmbedding.exception)
            is Result.Success -> {
                try {
                    val vecQuery = SimpleSQLiteQuery(
                        "SELECT rowid, distance FROM vec_memories WHERE embedding MATCH ? ORDER BY distance LIMIT ?",
                        arrayOf(embeddingService.floatArrayToByteArray(queryEmbedding.data), limit)
                    )
                    val vecResults = semanticMemoryDao.vectorSearch(vecQuery)
                    val ids = vecResults.map { it.rowid.toString() }
                    val memories = semanticMemoryDao.getByIds(ids)

                    memories.forEach {
                        semanticMemoryDao.markAccessed(it.id, System.currentTimeMillis())
                    }

                    val result = memories.map { entity ->
                        Memory(
                            id = entity.id,
                            type = MemoryType.SEMANTIC,
                            content = entity.content,
                            importance = entity.importance,
                            timestamp = entity.createdAt
                        )
                    }
                    Result.success(result)
                } catch (e: Exception) {
                    fallbackSearch(queryEmbedding.data, limit, minSimilarity)
                }
            }
        }
    }

    private suspend fun fallbackSearch(
        queryVector: FloatArray,
        limit: Int = 5,
        minSimilarity: Float = 0.65f
    ): Result<List<Memory>> {
        val allMemories = semanticMemoryDao.getMostImportant(100)
        val scored = allMemories.mapNotNull { entity ->
            val storedVector = embeddingService.byteArrayToFloatArray(entity.embedding)
            val similarity = embeddingService.cosineSimilarity(queryVector, storedVector)
            if (similarity >= minSimilarity) {
                entity to similarity
            } else null
        }
            .sortedByDescending { it.second }
            .take(limit)

        val memories = scored.map { (entity, _) ->
            Memory(
                id = entity.id,
                type = MemoryType.SEMANTIC,
                content = entity.content,
                importance = entity.importance,
                timestamp = entity.createdAt
            )
        }

        scored.forEach { (entity, _) ->
            semanticMemoryDao.markAccessed(entity.id, System.currentTimeMillis())
        }

        return Result.success(memories)
    }

    suspend fun getRecentEpisodes(limit: Int = 10): List<Memory> {
        return episodicMemoryDao.getRecent(limit).map { entity ->
            Memory(
                id = entity.id,
                type = MemoryType.EPISODIC,
                content = entity.summary,
                importance = entity.importance,
                timestamp = entity.timestamp
            )
        }
    }

    suspend fun getRecentConversationSummary(sessionId: String): String? {
        val episodes = episodicMemoryDao.getRecent(10)
        val sessionEpisodes = episodes.filter { it.sessionId == sessionId }
        if (sessionEpisodes.isEmpty()) return null
        return sessionEpisodes.joinToString("\n") { "- ${it.summary}" }
    }

    suspend fun deleteMemory(id: String, type: MemoryType) {
        when (type) {
            MemoryType.SEMANTIC -> {
                semanticMemoryDao.delete(id)
                try {
                    semanticMemoryDao.deleteVector(id)
                } catch (e: Exception) {
                }
            }
            MemoryType.EPISODIC -> episodicMemoryDao.delete(id)
            MemoryType.PROCEDURAL -> {}
        }
    }

    suspend fun getAllSemanticMemories(): List<Memory> {
        return semanticMemoryDao.getMostImportant(500).map { entity ->
            Memory(
                id = entity.id,
                type = MemoryType.SEMANTIC,
                content = entity.content,
                importance = entity.importance,
                timestamp = entity.createdAt
            )
        }
    }

    suspend fun runConsolidation() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        semanticMemoryDao.deleteDecayed(threshold = 3, before = thirtyDaysAgo)
        semanticMemoryDao.getOldest(
            System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000,
            50
        ).forEach { entity ->
            try {
                semanticMemoryDao.deleteVector(entity.id)
            } catch (e: Exception) {}
            semanticMemoryDao.delete(entity.id)
        }
    }

    suspend fun extractAndStoreFacts(
        messages: List<com.androidassistant.core.model.Message>,
        sessionId: String
    ): Result<FactExtractionUseCase.ExtractionResult> {
        if (factExtraction == null) return Result.success(FactExtractionUseCase.ExtractionResult(emptyList(), ""))

        val result = factExtraction.extractFromConversation(messages, sessionId)
        return when (result) {
            is Result.Error -> Result.error(result.exception)
            is Result.Success -> {
                val extraction = result.data
                for (fact in extraction.facts) {
                    if (fact.confidence >= 0.6f) {
                        val importance = (fact.confidence * 10).toInt().coerceIn(1, 10)
                        storeSemantic(
                            content = fact.content,
                            category = fact.category.lowercase(),
                            source = if (fact.confidence >= 0.8f) "explicit" else "inferred",
                            importance = importance,
                            metadata = mapOf(
                                "entities" to fact.entities.joinToString(","),
                                "session_id" to sessionId
                            )
                        )
                    }
                }
                Result.success(extraction)
            }
        }
    }

    suspend fun getMemoryCount(): Int = semanticMemoryDao.count()
}