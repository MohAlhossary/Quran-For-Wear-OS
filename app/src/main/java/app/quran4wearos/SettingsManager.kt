// SettingsManager.kt
package app.quran4wearos

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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

// Update the DataStore initialization with migration
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "quran_settings",
    produceMigrations = { context ->
        listOf(
            FontSizeMigration()
        )
    }
)

object SettingsManager {
    // Store Application context safely (Application context is safe for singletons)
    private lateinit var appContext: Context
    private val _settings = getDefaultSettingsFlow()
    val settings: StateFlow<SettingsData> = _settings.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val currentSettings: SettingsData
        get() = _settings.value

    fun initialize(context: Context) {
        // Store only the Application context, not Activity context
        appContext = context.applicationContext
        val dataStore = SettingsDataStore(appContext)

        // Load saved settings - migration will automatically handle the conversion
        managerScope.launch {
            dataStore.loadSettings().collect { savedSettings ->
                _settings.value = savedSettings
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _settings.update { it.copy(darkMode = enabled) }
        managerScope.launch {
            SettingsDataStore(appContext).setDarkMode(enabled)
        }
    }

    fun setFontSize(size: Int) {
        _settings.update { it.copy(fontSize = size) }
        managerScope.launch {
            SettingsDataStore(appContext).setFontSize(size)
        }
    }

    fun setLanguage(lang: String) {
        _settings.update { it.copy(language = lang) }
        managerScope.launch {
            SettingsDataStore(appContext).setLanguage(lang)
        }
    }

    // Optional: Clean up scope when needed
    fun cleanup() {
        managerScope.cancel()
    }
}

private fun getDefaultSettingsFlow(): MutableStateFlow<SettingsData> = MutableStateFlow(
    SettingsData(
        darkMode = true,
        fontSize = 15,
        language = getDeviceDefaultLanguage()
    )
)

data class SettingsData(
    val darkMode: Boolean,
    val fontSize: Int,
    val language: String
)

class SettingsDataStore(private val context: Context) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = intPreferencesKey("font_size")
        val LANGUAGE = stringPreferencesKey("language")

        // Old key for migration (kept for reference, but not used directly in code)
        private val OLD_FONT_SIZE = floatPreferencesKey("font_size")
    }

    suspend fun loadSettings(): kotlinx.coroutines.flow.Flow<SettingsData> {
        val defaults = getDefaultSettingsFlow()
        return context.dataStore.data.map { preferences ->
            SettingsData(
                darkMode = preferences[DARK_MODE] ?: defaults.value.darkMode,
                fontSize = preferences[FONT_SIZE] ?: defaults.value.fontSize,
                language = preferences[LANGUAGE] ?: defaults.value.language
            )
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    suspend fun setFontSize(size: Int) {
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

// Custom migration class for handling Float to Int conversion
class FontSizeMigration : DataMigration<Preferences> {

    override suspend fun cleanUp() {
        // No cleanup needed
    }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val oldKey = floatPreferencesKey("font_size")
        val newKey = intPreferencesKey("font_size")

        // Check if old Float key exists and new Int key doesn't
        return currentData.contains(oldKey) && !currentData.contains(newKey)
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutablePreferences = currentData.toMutablePreferences()

        val oldKey = floatPreferencesKey("font_size")
        val newKey = intPreferencesKey("font_size")

        // Get the old Float value
        val oldFontSize = currentData[oldKey]

        if (oldFontSize != null) {
            // Convert Float to Int (using toInt() truncates decimal places)
            // If you want rounding instead, use: oldFontSize.roundToInt()
            val newFontSize = oldFontSize.toInt() * 5

            // Save with new Int key
            mutablePreferences[newKey] = newFontSize

            // Remove the old Float key to clean up
            mutablePreferences.remove(oldKey)
        }

        return mutablePreferences.toPreferences()
    }
}