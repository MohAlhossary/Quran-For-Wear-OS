// SettingsManager.kt
package app.quran4wearos

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "quran_settings")

object SettingsManager {
    private lateinit var dataStore: SettingsDataStore
    private val _settings = MutableStateFlow(SettingsData(
        darkMode = true,
        fontSize = 1f,
        language = "EN"
    ))
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    // Create a dedicated coroutine scope for SettingsManager
    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // For non-flow access
    val currentSettings: SettingsData
        get() = _settings.value

    fun initialize(context: Context) {
        dataStore = SettingsDataStore(context)

        // Load saved settings
        managerScope.launch {
            dataStore.loadSettings().collect { savedSettings ->
                _settings.value = savedSettings
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _settings.update { it.copy(darkMode = enabled) }
        managerScope.launch {
            dataStore.setDarkMode(enabled)
        }
    }

    fun setFontSize(size: Float) {
        _settings.update { it.copy(fontSize = size) }
        managerScope.launch {
            dataStore.setFontSize(size)
        }
    }

    fun setLanguage(lang: String) {
        _settings.update { it.copy(language = lang) }
        managerScope.launch {
            dataStore.setLanguage(lang)
        }
    }

    // Optional: Clean up scope when needed
    fun cleanup() {
        managerScope.cancel()
    }
}

data class SettingsData(
    val darkMode: Boolean,
    val fontSize: Float,
    val language: String
)

class SettingsDataStore(private val context: Context) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val LANGUAGE = stringPreferencesKey("language")
    }

    suspend fun loadSettings(): kotlinx.coroutines.flow.Flow<SettingsData> {
        return context.dataStore.data.map { preferences ->
            SettingsData(
                darkMode = preferences[DARK_MODE] ?: true,
                fontSize = preferences[FONT_SIZE] ?: 1f,
                language = preferences[LANGUAGE] ?: "EN"
            )
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = lang
        }
    }
}