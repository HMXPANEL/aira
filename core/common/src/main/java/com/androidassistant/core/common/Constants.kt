package com.androidassistant.core.common

object Constants {
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    const val GEMINI_MODEL_FLASH = "gemini-2.0-flash"
    const val GEMINI_MODEL_PRO = "gemini-2.0-pro"

    const val MAX_AGENT_ITERATIONS = 10
    const val MAX_TOOL_TIMEOUT_MS = 30_000L
    const val MAX_MESSAGE_HISTORY = 50
    const val MAX_CONTEXT_TOKENS = 128_000

    const val PREFS_FILE_NAME = "assistant_preferences"
    const val KEY_GEMINI_API_KEY = "gemini_api_key"
    const val KEY_SAFETY_MODE = "safety_mode"
    const val KEY_FIRST_LAUNCH = "first_launch"

    const val NOTIFICATION_CHANNEL_ID = "assistant_foreground"
    const val NOTIFICATION_ID = 1

    const val DEFAULT_SAFETY_MODE = "balanced"
}
