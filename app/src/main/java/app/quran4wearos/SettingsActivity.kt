// Main Settings Activity
package app.quran4wearos

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.scrollAway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsActivity : ComponentActivity() {

    @OptIn(ExperimentalWearFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val language = SettingsManager.settings.collectAsState().value.language

            CompositionLocalProvider(
                LocalLayoutDirection provides when (language) {
                    "AR" -> LayoutDirection.Rtl
                    else -> LayoutDirection.Ltr
                },
            ) {
                SettingsScreen()
            }
        }
    }
}

// Helper function to get string in a specific language
@Composable
fun getStringInLanguage(@StringRes id: Int, languageCode: String): String {
    val context = LocalContext.current
    return remember(languageCode, id) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag(languageCode))
        context.createConfigurationContext(configuration).resources.getString(id)
    }
}

// Settings Screen
@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState: SettingsState by settingsViewModel.state.collectAsState()
    val context = LocalContext.current

    // Create a scrollable state for the scrollAway modifier
    val scrollableState = remember { ScrollableState { 0f } }

    // Sample QuranEntry for preview
    val sampleEntry = remember {
        QuranEntry(
            id = 1,
            suraNameAr = "الفاتحة",
            ayaNo = 1,
            page = 1,
            text = "\u200F\uE8DB \u200F\uE338\u200F\uE48E \u200F\uE338\u200F\uE0AF\u200F\uE238\u200F\uE903 \u200F\uE338\u200F\uE0AF\u200F\uE238\u200F\uE045\u200F\uE1C0\u200F\uE2E5 \u200F\uE95A",
            textEmlaey = "بسم الله الرحمن الرحيم"
        )
    }

    // Determine current language code for string resources
    val currentLanguageCode = remember(settingsState.language) {
        when (settingsState.language) {
            "AR" -> "ar"
            "EN" -> "en"
            else -> {
                val deviceLang = getDeviceDefaultLanguage()
                if (deviceLang == "AR") "ar" else "en"
            }
        }
    }

    // Scaffold for basic Wear OS structure
    Scaffold(
        timeText = { TimeText(modifier = Modifier.scrollAway { scrollableState }) },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        // Use Wear Compose's built-in list state
        val listState = rememberScalingLazyListState()

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                ListHeader {
                    Text(
                        getStringInLanguage(R.string.settings_title, currentLanguageCode),
                        style = MaterialTheme.typography.title2
                    )
                }
            }

            // Language Setting with Expandable Radio Buttons
            item {
                ExpandableSettingRadioButton(
                    icon = R.drawable.ic_language,
                    title = getStringInLanguage(R.string.settings_language, currentLanguageCode),
                    summary = settingsState.languageDisplay,
                    isExpanded = settingsState.isLanguageExpanded,
                    onExpandChange = { settingsViewModel.toggleLanguageExpanded() }
                ) {
                    RadioSettingItem(
                        icon = R.drawable.ic_language,
                        title = getStringInLanguage(R.string.english, currentLanguageCode),
                        selected = settingsState.language == "EN",
                        onClick = {
                            settingsViewModel.setLanguage("EN")
                            settingsViewModel.toggleLanguageExpanded()
                            // Apply locale change
                            applyLocale(context, "EN")
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    RadioSettingItem(
                        icon = R.drawable.ic_language,
                        title = getStringInLanguage(R.string.arabic, currentLanguageCode),
                        selected = settingsState.language == "AR",
                        onClick = {
                            settingsViewModel.setLanguage("AR")
                            settingsViewModel.toggleLanguageExpanded()
                            // Apply locale change
                            applyLocale(context, "AR")
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    RadioSettingItem(
                        icon = R.drawable.ic_language,
                        title = getStringInLanguage(R.string.device_default, currentLanguageCode),
                        selected = settingsState.language == "DEFAULT",
                        onClick = {
                            settingsViewModel.setLanguage("DEFAULT")
                            settingsViewModel.toggleLanguageExpanded()
                            // Apply locale change (device default)
                            applyLocale(context, "DEFAULT")
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Font Size Setting with Integer Slider
            item {
                IntegerSliderSettingItem(
                    title = getStringInLanguage(R.string.settings_font_size, currentLanguageCode),
                    value = settingsState.fontSize,
                    onValueChange = { settingsViewModel.setFontSize(it) },
                    valueRange = 5..35
                )
            }

            // Preview of AyaCard with current settings
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = getStringInLanguage(R.string.settings_preview, currentLanguageCode),
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    AyaCard(
                        entry = sampleEntry,
                        fontSize = (settingsState.fontSize).sp,
                        isDarkMode = settingsState.darkMode,
                    )
                }
            }

//            // Dark Mode Setting
//            item {
//                SwitchSettingItem(
//                    icon = R.drawable.ic_dark_mode,
//                    title = getStringInLanguage(R.string.settings_dark_mode, currentLanguageCode),
//                    isChecked = settingsState.darkMode,
//                    onCheckedChange = { settingsViewModel.toggleDarkMode() }
//                )
//            }
        }
    }
}

// Helper function to apply locale changes
fun applyLocale(context: android.content.Context, languageCode: String) {
    val locale = when (languageCode) {
        "AR" -> Locale.forLanguageTag("ar")
        "EN" -> Locale.forLanguageTag("en")
        else -> {
            // For device default, use the system locale
            val deviceLang = getDeviceDefaultLanguage()
            if (deviceLang == "AR") Locale.forLanguageTag("ar") else Locale.forLanguageTag("en")
        }
    }

    // Set the application locale using LocaleListCompat
    val localeList = LocaleListCompat.create(locale)
    AppCompatDelegate.setApplicationLocales(localeList)
}

// Custom Setting Items
@Composable
fun SwitchSettingItem(
    icon: Int,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ChipSettingItem(
    icon: Int,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1
                )
            }
            if (value != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.secondary
                    )
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun ExpandableSettingItem(
    icon: Int,
    title: String,
    summary: String? = null,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onExpandChange(!isExpanded) }
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.body1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (summary != null) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = if (isExpanded) R.drawable.ic_expand_less
                            else R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded content
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun ExpandableSettingRadioButton(
    icon: Int,
    title: String,
    summary: String? = null,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onExpandChange(!isExpanded) }
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.body1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (summary != null) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = if (isExpanded) R.drawable.ic_expand_less
                            else R.drawable.ic_expand_more
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded content
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

// Integer Slider Setting Item
@Composable
fun IntegerSliderSettingItem(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange
) {
    var sliderValue by remember { mutableIntStateOf(value) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(
                color = MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(4.dp),
        onClick = {}
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "$sliderValue",
                style = MaterialTheme.typography.title3,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = sliderValue.toFloat(),
                onValueChange = {
                    sliderValue = it.toInt()
                    onValueChange(sliderValue)
                },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = valueRange.last - valueRange.first - 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

// Dialogs
@Composable
fun ScreenTimeoutDialog(
    currentTimeout: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val currentLanguageCode = remember { "en" } // You might want to get this from settings

    val timeouts = listOf(
        getStringInLanguage(R.string.timeout_15_seconds, currentLanguageCode),
        getStringInLanguage(R.string.timeout_30_seconds, currentLanguageCode),
        getStringInLanguage(R.string.timeout_1_minute, currentLanguageCode),
        getStringInLanguage(R.string.timeout_2_minutes, currentLanguageCode),
        getStringInLanguage(R.string.timeout_5_minutes, currentLanguageCode)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getStringInLanguage(R.string.screen_timeout, currentLanguageCode)) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(timeouts) { timeout ->
                    Chip(
                        onClick = { onConfirm(timeout) },
                        label = { Text(timeout) },
                        colors = if (timeout == currentTimeout)
                            ChipDefaults.primaryChipColors()
                        else ChipDefaults.secondaryChipColors()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(getStringInLanguage(R.string.cancel, currentLanguageCode))
            }
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguageCode = remember { SettingsManager.settings.value.language } // You might want to get this from settings

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getStringInLanguage(R.string.about, currentLanguageCode)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_watch),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = getStringInLanguage(R.string.wear_os_watch, currentLanguageCode),
                    style = MaterialTheme.typography.title2,
                    color = Color.Black
                )
                Text(
                    text = getStringInLanguage(R.string.model, currentLanguageCode),
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
                Text(
                    text = getStringInLanguage(R.string.os_version, currentLanguageCode),
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
                Text(
                    text = getStringInLanguage(R.string.build, currentLanguageCode),
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(getStringInLanguage(R.string.ok, currentLanguageCode))
            }
        }
    )
}

@Composable
fun RadioSettingItem(
    icon: Int,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1
                )
            }

            RadioButton(
                selected = selected,
                onClick = onClick,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


// Helper function to get device default language
fun getDeviceDefaultLanguage(): String {
    val deviceLanguage = Locale.getDefault().language
    println("Device language = $deviceLanguage")
    return when {
        deviceLanguage.equals("ar", ignoreCase = true) -> "AR"
        else -> "EN"
    }
}

// UPDATED ViewModel with SettingsManager integration and device default language fallback
class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        SettingsState(
            darkMode = SettingsManager.currentSettings.darkMode,
            fontSize = SettingsManager.currentSettings.fontSize,
            language = SettingsManager.currentSettings.language.ifEmpty { "DEFAULT" },
            isLanguageExpanded = false
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (SettingsManager.currentSettings.language.isEmpty()) {
                val deviceDefaultLanguage = getDeviceDefaultLanguage()
                SettingsManager.setLanguage(deviceDefaultLanguage)
            }
        }

        viewModelScope.launch {
            SettingsManager.settings.collect { settings ->
                _state.update { currentState ->
                    currentState.copy(
                        darkMode = settings.darkMode,
                        fontSize = settings.fontSize,
                        language = settings.language.ifEmpty { "DEFAULT" }
                    )
                }
            }
        }
    }

    fun toggleDarkMode() {
        SettingsManager.setDarkMode(!_state.value.darkMode)
    }

    fun setFontSize(fontSize: Int) {
        SettingsManager.setFontSize(fontSize)
    }

    fun setLanguage(language: String) {
        val languageToSet = if (language == "DEFAULT") {
            getDeviceDefaultLanguage()
        } else {
            language
        }
        SettingsManager.setLanguage(languageToSet)
    }

    fun toggleLanguageExpanded() {
        _state.update { currentState ->
            currentState.copy(isLanguageExpanded = !currentState.isLanguageExpanded)
        }
    }
}

// UPDATED data class with display helper using getStringInLanguage
data class SettingsState(
    val darkMode: Boolean,
    val fontSize: Int,
    val language: String,
    val isLanguageExpanded: Boolean = false
) {
    val languageDisplay: String
        @Composable
        get() {
            val currentLanguageCode = when (language) {
                "AR" -> "ar"
                "EN" -> "en"
                else -> {
                    val deviceLang = getDeviceDefaultLanguage()
                    if (deviceLang == "AR") "ar" else "en"
                }
            }

            return when (language) {
                "EN" -> getStringInLanguage(R.string.english, currentLanguageCode)
                "AR" -> getStringInLanguage(R.string.arabic, currentLanguageCode)
                "DEFAULT" -> {
                    val deviceLang = getDeviceDefaultLanguage()
                    val deviceLangDisplay = if (deviceLang == "AR")
                        getStringInLanguage(R.string.arabic, currentLanguageCode)
                    else
                        getStringInLanguage(R.string.english, currentLanguageCode)
                    "${getStringInLanguage(R.string.device_default, currentLanguageCode)} ($deviceLangDisplay)"
                }
                else -> language
            }
        }
}

// Preview functions
@OptIn(ExperimentalHorologistApi::class)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewExpandableSettingItem() {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item {
            ExpandableSettingItem(
                icon = R.drawable.ic_watch,
                title = "Font preview",
                isExpanded = false,
                onExpandChange = { },
                content = {
                    Text("Content")
                }
            )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PreviewTest() {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = listState
    ) {
        item {
            ExpandableSettingItem(
                icon = R.drawable.ic_volume,
                title = "Sound & vibration",
                summary = "Preview",
                isExpanded = true,
                onExpandChange = {}
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IntegerSliderSettingItem(
                        title = "Volume",
                        value = 50,
                        onValueChange = {},
                        valueRange = 0..100
                    )

                    SwitchSettingItem(
                        icon = R.drawable.ic_vibrate,
                        title = "Vibrate",
                        isChecked = true,
                        onCheckedChange = {}
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}

//@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
//@Composable
//fun previewAyaCard() {
//    AyaCard(
//        QuranEntry(),
//        c
//    )
//}