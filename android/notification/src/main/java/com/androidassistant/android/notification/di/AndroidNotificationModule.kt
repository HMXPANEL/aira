package com.androidassistant.android.notification.di

import android.content.Context
import com.androidassistant.android.notification.NotificationTools
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidNotificationModule = module {
    single { NotificationTools(androidContext()) }
}