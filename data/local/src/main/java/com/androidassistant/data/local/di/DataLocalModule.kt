package com.androidassistant.data.local.di

import com.androidassistant.data.local.AppDatabase
import com.androidassistant.data.local.preferences.UserPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataLocalModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().conversationDao() }
    single { get<AppDatabase>().toolExecutionDao() }
    single { UserPreferences(androidContext()) }
}
