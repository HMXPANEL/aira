package com.androidassistant.agent.memory

import com.androidassistant.core.common.Result
import com.androidassistant.core.model.Memory
import com.androidassistant.core.model.MemoryType
import com.androidassistant.data.memory.dao.EpisodicMemoryDao
import com.androidassistant.data.memory.dao.SemanticMemoryDao
import com.androidassistant.data.memory.embedding.EmbeddingService
import com.androidassistant.data.memory.entity.EpisodicMemoryEntity
import com.androidassistant.data.memory.entity.SemanticMemoryEntity
import java.util.UUID

class MemoryManager(
    private val semanticMemoryDao: SemanticMemoryDao,
    private val episodicMemoryDao: EpisodicMemoryDao,
    private val embeddingService: EmbeddingService
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
                val memory = SemanticMemoryEntity(
                    id = UUID.randomUUID().toString(),
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
                val allMemories = semanticMemoryDao.getMostImportant(100)
                val scored = allMemories.mapNotNull { entity ->
                    val storedVector = embeddingService.byteArrayToFloatArray(entity.embedding)
                    val similarity = embeddingService.cosineSimilarity(queryEmbedding.data, storedVector)
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

                Result.success(memories)
            }
        }
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
            MemoryType.SEMANTIC -> semanticMemoryDao.delete(id)
            MemoryType.EPISODIC -> episodicMemoryDao.delete(id)
            MemoryType.PROCEDURAL -> { /* TBD */ }
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
        val decayed = semanticMemoryDao.deleteDecayed(threshold = 3, before = thirtyDaysAgo)
        val archived = episodicMemoryDao.deleteOlderThan(
            System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
        )
    }

    suspend fun getMemoryCount(): Int = semanticMemoryDao.count()
}
