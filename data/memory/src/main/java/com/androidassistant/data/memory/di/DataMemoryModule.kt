package com.androidassistant.data.memory.di

import com.androidassistant.data.local.preferences.UserPreferences
import com.androidassistant.data.memory.MemoryDatabase
import com.androidassistant.data.memory.embedding.EmbeddingService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataMemoryModule = module {
    single { MemoryDatabase.create(androidContext()) }
    single { get<MemoryDatabase>().semanticMemoryDao() }
    single { get<MemoryDatabase>().episodicMemoryDao() }

    single {
        val prefs: UserPreferences = get()
        val apiKey = runBlocking { prefs.geminiApiKey.first() } ?: ""
        EmbeddingService(apiKey)
    }
}
