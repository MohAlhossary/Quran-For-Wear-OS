package app.quran4wearos

// WEAR MATERIAL - UI Components

// WEAR FOUNDATION - The List Logic (Fixes the ambiguity error)


import android.content.ClipData
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import app.quran4wearos.ui.theme.HafsSmartFontFamily
import kotlinx.coroutines.delay


class QuranViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetAyaId = intent.getIntExtra("EXTRA_AYA_ID", -1)

        setContent {
            val repository = remember { QuranRepository(applicationContext) }
            val viewModel: QuranViewModel = viewModel(factory = QuranViewModelFactory(repository))
            val entries by viewModel.displayList.collectAsState()

            // Fixed ambiguity by ensuring foundation import
            val listState = rememberScalingLazyListState()
            var useEmlaey by remember { mutableStateOf(false) }

            LaunchedEffect(targetAyaId) {
                if (targetAyaId != -1) viewModel.loadFromAyaId(targetAyaId)
            }

            LaunchedEffect(entries) {
                if (targetAyaId != -1 && entries.isNotEmpty()) {
                    val index = entries.indexOfFirst { it.id == targetAyaId }
                    if (index != -1) {
                        delay(100)
                        listState.animateScrollToItem(index)
                    }
                }
            }

            Scaffold(
                timeText = { TimeText() },
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
            ) {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(), // Error was here
                    state = listState,
                    autoCentering = AutoCenteringParams(itemIndex = 0)
                ) {
                    item {
                        ToggleChip(
                            label = { Text("Emlaey Script") },
                            secondaryLabel = { Text(if (useEmlaey) "On" else "Off") },
                            checked = useEmlaey,
                            onCheckedChange = { viewModel.toggleScript() },
                            // Use named parameters for the Switch to avoid ambiguity
                            toggleControl = {
                                Switch(
                                    checked = useEmlaey,
                                    onCheckedChange = { viewModel.toggleScript() }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // TODO placeholder for jump to aya button

                    items(entries, key = { it.id }) { entry ->
                        AyaCard(entry, useEmlaey)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AyaCard(entry: QuranEntry, useEmlaey: Boolean) {
    val clipboard = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val textToCopy = if (useEmlaey) entry.textEmlaey else entry.text
    val context = LocalContext.current
    // Force RTL for Arabic content
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Card(
            onClick = { /* Select Aya logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* Primary action */ },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        clipboard.setText(AnnotatedString(textToCopy))
                        Toast.makeText(context, "تم النسخ", Toast.LENGTH_SHORT).show()
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Sura Name and Aya Number
                Text(
                    text = "${entry.suraNameAr} (${entry.ayaNo})",
                    style = MaterialTheme.typography.caption2,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Main Quran Text
                Text(
                    text = textToCopy,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1.copy(
                        // Ensures punctuation like periods/brackets stay on the left in RTL
                        textDirection = TextDirection.Rtl
                    ),
                    fontFamily = HafsSmartFontFamily,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}