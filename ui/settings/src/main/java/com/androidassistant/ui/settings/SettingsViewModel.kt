package com.androidassistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidassistant.data.local.preferences.UserPreferences
import com.androidassistant.data.remote.gemini.GeminiApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val geminiApi: GeminiApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val apiKey = userPreferences.geminiApiKey.first() ?: ""
            val safetyMode = userPreferences.safetyMode.first()
            _uiState.update {
                it.copy(
                    geminiApiKey = apiKey,
                    safetyMode = safetyMode
                )
            }
        }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(geminiApiKey = key) }
    }

    fun updateSafetyMode(mode: String) {
        _uiState.update { it.copy(safetyMode = mode) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            userPreferences.setGeminiApiKey(_uiState.value.geminiApiKey)
            userPreferences.setSafetyMode(_uiState.value.safetyMode)
            geminiApi.updateApiKey(_uiState.value.geminiApiKey)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    savedMessage = "Settings saved"
                )
            }
        }
    }

    fun clearSavedMessage() {
        _uiState.update { it.copy(savedMessage = null) }
    }
}
