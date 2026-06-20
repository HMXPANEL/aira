package com.androidassistant.android.foreground.di

import com.androidassistant.android.foreground.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidForegroundModule = module {
    single { NotificationHelper(androidContext()) }
}
