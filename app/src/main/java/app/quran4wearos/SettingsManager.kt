// SettingsManager.kt
package app.quran4wearos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quran_settings"
)

// Define keys
val DARK_MODE = booleanPreferencesKey("dark_mode")
val FONT_SIZE_INT = intPreferencesKey("font_size")  // New Int key
val FONT_SIZE_FLOAT = floatPreferencesKey("font_size")  // Old Float key (keep for compatibility)
val LANGUAGE = stringPreferencesKey("language")

object SettingsManager {
    private lateinit var appContext: Context
    private val _settings = MutableStateFlow(
        SettingsData(
            darkMode = true,
            fontSize = 15,
            language = "EN"
        )
    )
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val currentSettings: SettingsData
        get() = _settings.value

    fun initialize(context: Context) {
        appContext = context.applicationContext

        managerScope.launch {
            try {
                // Collect settings with error handling
                appContext.dataStore.data
                    .catch { exception ->
                        // If there's an error reading data, use defaults
                        exception.printStackTrace()
                        emit(emptyPreferences())
                    }
                    .collect { preferences ->
                        try {
                            // Safely get dark mode
                            val darkMode = try {
                                preferences[DARK_MODE] ?: true
                            } catch (e: Exception) {
                                true
                            }

                            // Safely get font size
                            val fontSize = try {
                                // First try Int key
                                val intSize = preferences[FONT_SIZE_INT]
                                if (intSize != null) {
                                    intSize
                                } else {
                                    // Then try Float key
                                    val floatSize = preferences[FONT_SIZE_FLOAT]
                                    if (floatSize != null) {
                                        val convertedSize = floatSize.toInt()
                                        // Save converted value in background
                                        saveConvertedFontSize(convertedSize)
                                        convertedSize
                                    } else {
                                        15
                                    }
                                }
                            } catch (e: Exception) {
                                15
                            }

                            // Safely get language
                            val language = try {
                                preferences[LANGUAGE] ?: "EN"
                            } catch (e: Exception) {
                                "EN"
                            }

                            // Update settings
                            _settings.value = SettingsData(
                                darkMode = darkMode,
                                fontSize = fontSize,
                                language = language
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Keep existing settings on error
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                // Keep default settings on error
            }
        }
    }

    private fun saveConvertedFontSize(size: Int) {
        managerScope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences[FONT_SIZE_INT] = size
                    // Remove old key after conversion
                    preferences.remove(FONT_SIZE_FLOAT)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _settings.update { it.copy(darkMode = enabled) }
        managerScope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences[DARK_MODE] = enabled
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun setFontSize(size: Int) {
        _settings.update { it.copy(fontSize = size) }
        managerScope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences[FONT_SIZE_INT] = size
                    // Clean up old key
                    preferences.remove(FONT_SIZE_FLOAT)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun setLanguage(lang: String) {
        _settings.update { it.copy(language = lang) }
        managerScope.launch {
            try {
                appContext.dataStore.edit { preferences ->
                    preferences[LANGUAGE] = lang
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun cleanup() {
        managerScope.cancel()
    }
}

data class SettingsData(
    val darkMode: Boolean,
    val fontSize: Int,
    val language: String
)