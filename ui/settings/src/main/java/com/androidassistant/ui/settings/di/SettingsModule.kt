package com.androidassistant.ui.settings.di

import com.androidassistant.data.local.preferences.UserPreferences
import com.androidassistant.data.remote.gemini.GeminiApi
import com.androidassistant.ui.settings.SettingsViewModel
import org.koin.dsl.module

val settingsModule = module {
    factory { SettingsViewModel(get<UserPreferences>(), get<GeminiApi>()) }
}
