// Main Settings Activity
package app.quran4wearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.scrollAway
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    @OptIn(ExperimentalWearFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen()
        }
    }
}

// Settings Screen
@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settingsState: SettingsState by settingsViewModel.state.collectAsState()

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
                ListHeader { Text("Settings", style = MaterialTheme.typography.title2) }
            }

            // Language Setting with Expandable Radio Buttons
            item {
                ExpandableSettingRadioButton(
                    icon = R.drawable.ic_language,
                    title = "Language ",
                    summary = settingsState.language,
                    isExpanded = settingsState.isLanguageExpanded,
                    onExpandChange = { settingsViewModel.toggleLanguageExpanded() }
                ) {
                    RadioSettingItem(
                        icon = R.drawable.ic_language,
                        title = "English",
                        selected = settingsState.language == "EN",
                        onClick = {
                            settingsViewModel.setLanguage("EN")
                            settingsViewModel.toggleLanguageExpanded()
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    RadioSettingItem(
                        icon = R.drawable.ic_language,
                        title = "Arabic",
                        selected = settingsState.language == "AR",
                        onClick = {
                            settingsViewModel.setLanguage("AR")
                            settingsViewModel.toggleLanguageExpanded()
                        },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Font Size Setting with Slider
            item {
                SliderSettingItem(
                    title = "Font Size",
                    value = settingsState.fontSize,
                    onValueChange = { settingsViewModel.setFontSize(it) },
                    valueRange = 1f..7f
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
                        text = "Preview",
                        style = MaterialTheme.typography.caption2,
                        color = MaterialTheme.colors.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    AyaCard(
                        entry = sampleEntry,
                        fontSize = (5*settingsState.fontSize).sp,
                        isDarkMode = settingsState.darkMode,
                    )
                }
            }

//            // Dark Mode Setting
//            item {
//                SwitchSettingItem(
//                    icon = R.drawable.ic_dark_mode,
//                    title = "Dark Mode",
//                    isChecked = settingsState.darkMode,
//                    onCheckedChange = { settingsViewModel.toggleDarkMode() }
//                )
//            }
        }
    }
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

@Composable
fun SliderSettingItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Card (
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
        Text(
            text = title,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "${value.toInt()}",
            style = MaterialTheme.typography.title3,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = 15,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
    }
}

// Dialogs
@Composable
fun ScreenTimeoutDialog(
    currentTimeout: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val timeouts = listOf("15 seconds", "30 seconds", "1 minute", "2 minutes", "5 minutes")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Screen timeout") },
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
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About") },
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
                    text = "Wear OS Watch",
                    style = MaterialTheme.typography.title2,
                    color = Color.Black
                )
                Text(
                    text = "Model: WEAR-001",
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
                Text(
                    text = "OS Version: Wear OS 4.0",
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
                Text(
                    text = "Build: AP1A.240505.001",
                    style = MaterialTheme.typography.body2,
                    color = Color.Black
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
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

//// Preview AyaCard - renamed to avoid conflict
//@Composable
//fun PreviewAyaCard(
//    entry: QuranEntry,
//    fontSize: Float,
//    isDarkMode: Boolean
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        onClick = { }
//    ) {
//        Text(
//            fontFamily = HafsSmartFontFamily,
//            text = entry.text,
//            textAlign = TextAlign.Center,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp)
//                .background(
//                    color = if (isDarkMode)
//                        MaterialTheme.colors.surface
//                    else
//                        MaterialTheme.colors.primary.copy(alpha = 0.1f)
//                ),
//            fontSize = (5 * fontSize).sp,
//            color = if (isDarkMode)
//                MaterialTheme.colors.onSurface
//            else
//                MaterialTheme.colors.onPrimary
//        )
//    }
//}

// UPDATED ViewModel with SettingsManager integration
class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        SettingsState(
            darkMode = SettingsManager.currentSettings.darkMode,
            fontSize = SettingsManager.currentSettings.fontSize,
            language = SettingsManager.currentSettings.language,
            isLanguageExpanded = false
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        // Listen for changes from SettingsManager
        viewModelScope.launch {
            SettingsManager.settings.collect { settings ->
                _state.update { currentState ->
                    currentState.copy(
                        darkMode = settings.darkMode,
                        fontSize = settings.fontSize,
                        language = settings.language
                    )
                }
            }
        }
    }

    fun toggleDarkMode() {
        SettingsManager.setDarkMode(!_state.value.darkMode)
    }

    fun setFontSize(fontSize: Float) {
        SettingsManager.setFontSize(fontSize)
    }

    fun setLanguage(language: String) {
        SettingsManager.setLanguage(language)
    }

    fun toggleLanguageExpanded() {
        _state.update { currentState ->
            currentState.copy(isLanguageExpanded = !currentState.isLanguageExpanded)
        }
    }
}

// Updated data class with display helper
data class SettingsState(
    val darkMode: Boolean,
    val fontSize: Float,
    val language: String,
    val isLanguageExpanded: Boolean = false
) {
    val languageDisplay: String
        get() = when (language) {
            "EN" -> "English"
            "AR" -> "Arabic"
            else -> language
        }
}

// Preview functions
@OptIn(ExperimentalHorologistApi::class)
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
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

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
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
                    SliderSettingItem(
                        title = "Volume",
                        value = 50f,
                        onValueChange = {},
                        valueRange = 0f..100f
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

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}