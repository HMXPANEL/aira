package com.androidassistant.tool.registry.di

import com.androidassistant.tool.registry.ToolRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val toolRegistryModule = module {
    single { ToolRegistry(androidContext()) }
}
