package com.androidassistant.ui.settings

data class SettingsUiState(
    val geminiApiKey: String = "",
    val safetyMode: String = "balanced",
    val isSaving: Boolean = false,
    val savedMessage: String? = null
)
