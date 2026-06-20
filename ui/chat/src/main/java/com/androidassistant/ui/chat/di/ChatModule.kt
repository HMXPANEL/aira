package com.androidassistant.ui.chat.di

import com.androidassistant.agent.engine.AgentOrchestrator
import com.androidassistant.ui.chat.ChatViewModel
import org.koin.dsl.module

val chatModule = module {
    factory { ChatViewModel(get<AgentOrchestrator>()) }
}
