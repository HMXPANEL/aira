package com.androidassistant.android.overlay.di

import android.content.Context
import com.androidassistant.android.overlay.OverlayService
import org.koin.android.ext.android.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

val androidOverlayModule = module {
    single { OverlayService() }
}