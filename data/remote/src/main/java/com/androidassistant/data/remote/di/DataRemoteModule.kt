package com.androidassistant.data.remote.di

import com.androidassistant.data.local.preferences.UserPreferences
import com.androidassistant.data.remote.gemini.GeminiApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

val dataRemoteModule = module {
    single {
        val prefs: UserPreferences = get()
        val apiKey = runBlocking { prefs.geminiApiKey.first() } ?: ""
        GeminiApi(apiKey)
    }
}
