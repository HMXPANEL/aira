package com.androidassistant.ui.memory.di

import com.androidassistant.agent.memory.MemoryManager
import com.androidassistant.ui.memory.MemoryBrowserViewModel
import org.koin.dsl.module

val memoryBrowserModule = module {
    factory { MemoryBrowserViewModel(get<MemoryManager>()) }
}
