package com.androidassistant.ui.chat.di

import com.androidassistant.agent.engine.AgentOrchestrator
import com.androidassistant.agent.engine.safety.DefaultApprovalCallback
import com.androidassistant.ui.chat.ChatViewModel
import org.koin.dsl.module

val chatModule = module {
    single { DefaultApprovalCallback() }
    factory { ChatViewModel(get<AgentOrchestrator>(), get<DefaultApprovalCallback>()) }
}