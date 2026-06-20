package com.androidassistant.core.common

import android.util.Log

private const val TAG = "AndroidAssistant"

fun logD(message: String) {
    Log.d(TAG, message)
}

fun logE(message: String, throwable: Throwable? = null) {
    if (throwable != null) {
        Log.e(TAG, message, throwable)
    } else {
        Log.e(TAG, message)
    }
}

fun logW(message: String) {
    Log.w(TAG, message)
}

fun logI(message: String) {
    Log.i(TAG, message)
}