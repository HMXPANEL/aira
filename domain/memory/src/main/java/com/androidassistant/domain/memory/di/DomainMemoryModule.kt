package com.androidassistant.domain.memory.di

import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.domain.memory.ContradictionDetectionUseCase
import com.androidassistant.domain.memory.FactExtractionUseCase
import org.koin.dsl.module

val domainMemoryModule = module {
    single { FactExtractionUseCase(get<LLMProvider>()) }
    single { ContradictionDetectionUseCase(get<LLMProvider>()) }
}