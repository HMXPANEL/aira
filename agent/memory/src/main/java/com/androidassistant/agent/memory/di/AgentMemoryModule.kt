package com.androidassistant.agent.memory.di

import com.androidassistant.agent.memory.MemoryManager
import com.androidassistant.data.memory.dao.EpisodicMemoryDao
import com.androidassistant.data.memory.dao.SemanticMemoryDao
import com.androidassistant.data.memory.embedding.EmbeddingService
import com.androidassistant.domain.memory.ContradictionDetectionUseCase
import com.androidassistant.domain.memory.FactExtractionUseCase
import org.koin.dsl.module

val agentMemoryModule = module {
    single {
        MemoryManager(
            semanticMemoryDao = get<SemanticMemoryDao>(),
            episodicMemoryDao = get<EpisodicMemoryDao>(),
            embeddingService = get<EmbeddingService>(),
            factExtraction = getOrNull<FactExtractionUseCase>(),
            contradictionDetection = getOrNull<ContradictionDetectionUseCase>()
        )
    }
}