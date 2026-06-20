package com.androidassistant.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.androidassistant.core.common.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.PREFS_FILE_NAME
)

class UserPreferences(private val context: Context) {

    val geminiApiKey: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[GEMINI_API_KEY]
    }

    val safetyMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SAFETY_MODE] ?: Constants.DEFAULT_SAFETY_MODE
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FIRST_LAUNCH] ?: true
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[GEMINI_API_KEY] = key
        }
    }

    suspend fun setSafetyMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[SAFETY_MODE] = mode
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = false
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private val GEMINI_API_KEY = stringPreferencesKey(Constants.KEY_GEMINI_API_KEY)
        private val SAFETY_MODE = stringPreferencesKey(Constants.KEY_SAFETY_MODE)
        private val FIRST_LAUNCH = stringPreferencesKey(Constants.KEY_FIRST_LAUNCH)
    }
}
