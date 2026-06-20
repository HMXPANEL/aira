package com.androidassistant.agent.llm.di

import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.agent.llm.gemini.GeminiProvider
import com.androidassistant.data.remote.gemini.GeminiApi
import org.koin.dsl.module

val agentLlmModule = module {
    single<LLMProvider> { GeminiProvider(get<GeminiApi>()) }
}
