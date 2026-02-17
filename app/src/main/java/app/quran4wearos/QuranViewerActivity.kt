package app.quran4wearos

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
import kotlin.collections.find
import kotlin.collections.indexOfFirst
import kotlin.collections.isNotEmpty
import kotlin.collections.minByOrNull
import androidx.compose.runtime.collectAsState


class QuranViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetAyaId = intent.getIntExtra("EXTRA_AYA_ID", -1)

        setContent {
            val repository = remember { QuranRepository(applicationContext) }
            val viewModel: QuranViewModel = viewModel(factory = QuranViewModelFactory(repository))
            val entries by viewModel.displayList.collectAsState()
            val targetIndex by viewModel.targetInitialIndex.collectAsState()

            // Create list state
            val listState = rememberScalingLazyListState()

            // Track if we've done the initial positioning
            var initialPositionDone by remember { mutableStateOf(false) }

            // Load the initial data
            LaunchedEffect(targetAyaId) {
                if (targetAyaId != -1) {
                    viewModel.loadFromAyaId(targetAyaId)
                }
            }

            // Position the list at the target item without animation
            LaunchedEffect(targetIndex, entries, initialPositionDone) {
                if (targetIndex != -1 && entries.isNotEmpty() && !initialPositionDone) {
                    // Use scrollToItem instead of animateScrollToItem for instant positioning
                    listState.scrollToItem(targetIndex)
                    initialPositionDone = true
                    viewModel.clearTargetIndex()
                }
            }

            // Detect the current centered entry
            val currentCenterEntry by remember(entries, listState) {
                derivedStateOf {
                    val visibleItems = listState.layoutInfo.visibleItemsInfo
                    if (visibleItems.isNotEmpty()) {
                        // Offset 0 is the center-line in ScalingLazyColumn with autoCentering
                        val centerItem = visibleItems.minByOrNull { Math.abs(it.offset) }
                        val key = centerItem?.key
                        if (key is Int) entries.find { it.id == key } else null
                    } else null
                }
            }

            // Update adjacent pages as the user scrolls
            LaunchedEffect(currentCenterEntry?.id) {
                currentCenterEntry?.id?.let { id ->
                    viewModel.updateAdjacentPages(id)
                }
            }

            Scaffold(
                timeText = {
                    // Sura and Juzz Indicator using Curved Text
                    currentCenterEntry?.let { entry ->
                        TimeText(
                            startCurvedContent = {
                                curvedText("جزء ${repository.getMetadataByIdNotSafe(entry.id)?.jozz}")
                            },
                            endCurvedContent = {
                                curvedText(entry.suraNameAr)
                            }
                        )
                    }
                },
                vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
                positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
            ) {
                ScalingLazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    autoCentering = AutoCenteringParams(itemIndex = 0)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        AyaCard(
                            entry = entry,
                            useEmlaey = viewModel.useEmlaey
                        )
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
    val context = LocalContext.current
    val textToCopy = if (useEmlaey) entry.textEmlaey else entry.text
    // Force RTL for Arabic content
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Card(
            onClick = { /* Primary action */ },
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* Secondary action */ },
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
//                // Header: Sura Name and Aya Number
//                Text(
//                    text = "${entry.suraNameAr} (${entry.ayaNo})",
//                    style = MaterialTheme.typography.caption2,
//                    color = MaterialTheme.colors.secondary,
//                    textAlign = TextAlign.Center
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
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