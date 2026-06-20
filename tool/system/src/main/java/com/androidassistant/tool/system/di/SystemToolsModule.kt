package com.androidassistant.tool.system.di

import com.androidassistant.tool.registry.ToolRegistry
import com.androidassistant.tool.system.SystemTools
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val systemToolsModule = module {
    single { SystemTools(androidContext()) }

    onStart {
        val registry: ToolRegistry = get()
        val tools: SystemTools = get()

        registry.registerAll(
            listOf(
                tools.getDeviceInfoTool(),
                tools.getCurrentTimeTool(),
                tools.openAppTool(),
                tools.setTimerTool()
            )
        )
    }
}
