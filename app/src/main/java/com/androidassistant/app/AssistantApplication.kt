package com.androidassistant.app

import android.app.Application
import com.androidassistant.agent.engine.di.agentEngineModule
import com.androidassistant.agent.llm.di.agentLlmModule
import com.androidassistant.agent.memory.di.agentMemoryModule
import com.androidassistant.android.foreground.di.androidForegroundModule
import com.androidassistant.data.local.di.dataLocalModule
import com.androidassistant.data.memory.di.dataMemoryModule
import com.androidassistant.data.remote.di.dataRemoteModule
import com.androidassistant.tool.registry.di.toolRegistryModule
import com.androidassistant.tool.system.di.systemToolsModule
import com.androidassistant.ui.chat.di.chatModule
import com.androidassistant.ui.memory.di.memoryBrowserModule
import com.androidassistant.ui.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AssistantApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AssistantApplication)
            modules(
                // Core infrastructure
                dataLocalModule,
                dataRemoteModule,
                dataMemoryModule,

                // Agent
                agentLlmModule,
                agentEngineModule,
                agentMemoryModule,

                // Tools
                toolRegistryModule,
                systemToolsModule,

                // Android integration
                androidForegroundModule,

                // UI
                chatModule,
                settingsModule,
                memoryBrowserModule
            )
        }
    }
}
