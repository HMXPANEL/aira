package com.androidassistant.agent.engine.di

import com.androidassistant.agent.engine.AgentOrchestrator
import com.androidassistant.agent.engine.context.ContextAssembler
import com.androidassistant.agent.engine.safety.SafetyGate
import com.androidassistant.agent.llm.LLMProvider
import com.androidassistant.tool.registry.ToolRegistry
import org.koin.dsl.module

val agentEngineModule = module {
    single { ContextAssembler() }
    single { SafetyGate() }
    single {
        AgentOrchestrator(
            llmProvider = get<LLMProvider>(),
            contextAssembler = get(),
            toolRegistry = get(),
            safetyGate = get()
        )
    }
}
