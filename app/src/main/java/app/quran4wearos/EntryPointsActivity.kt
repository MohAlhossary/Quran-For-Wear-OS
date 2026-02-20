package app.quran4wearos

//import androidx.compose.foundation.layout.*
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import app.quran4wearos.ui.theme.HafsRegularFontFamily
import kotlinx.coroutines.launch

class EntryPointsActivity : ComponentActivity() {

    var textDirection: TextDirection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Note: For production, inject this via Dagger/Hilt or use a custom Application class
        val repository = QuranRepository(applicationContext)


        setContent {
            val language = SettingsManager.settings.collectAsState().value.language

            textDirection = if (SettingsManager.settings.collectAsState().value.language == "EN") {
                TextDirection.Ltr
            } else if (SettingsManager.settings.collectAsState().value.language == "AR") {
                TextDirection.Rtl
            } else {
                TODO()
            }
            CompositionLocalProvider(
                LocalLayoutDirection provides when (language) {
                    "AR" -> LayoutDirection.Rtl
                    else -> LayoutDirection.Ltr
                },
            ) {

                var quranData by remember { mutableStateOf<List<QuranListEntry>>(emptyList()) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        quranData = repository.getNavigationList()
                    }
                }

                MaterialTheme {
                    if (quranData.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        QuranMenuScreen(quranData)
                    }
                }
            }
        }
    }
}

@Composable
fun QuranMenuScreen(items: List<QuranListEntry>) {
    val listState = rememberScalingLazyListState()
    val context = LocalContext.current

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 30.dp)
        ) {
            items(items.size) { index ->
                val entry = items[index]
                QuranMenuItem(entry) {
                    val intent = Intent(context, QuranViewerActivity::class.java).apply {
//                        putExtra("EXTRA_PAGE", entry.page)
                        putExtra("EXTRA_AYA_ID", entry.id)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun QuranMenuItem(entry: QuranListEntry, onClick: () -> Unit) {
    val language = SettingsManager.settings.collectAsState().value.language
    TitleCard(
        onClick = onClick,
        title = {
            Text(
                text = if (language == "EN") {
                    if (entry.type == "S") "Sura ${entry.suraNameEn}"
                    else "Juzz ${entry.jozz}"
                } else if (language == "AR"){
                    if (entry.type == "S") "سورة " + entry.suraNameAr
                    else "جزء " + entry.jozz
                } else {
                    "" //FIXME bug material right here 😭😭
                },
                color = if (entry.type == "S") Color.Cyan else Color.Yellow,
                fontFamily = HafsRegularFontFamily,
                style = TextStyle(textDirection = when(language){ //This will only fix the text direction not alignment
                    "AR" -> TextDirection.Rtl
                    else -> TextDirection.Ltr
                }
                ),
                textAlign = when(entry.type){
                    "J" -> TextAlign.Center
                    else -> TextAlign.Start
                }
            )
        }
    ) {
        if (entry.type == "J") {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    entry.text,
                    style = MaterialTheme.typography.caption2,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth() //this is very important, otherwise you won't feel the RTL
                )
            }
        }    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewQuranMenuScreen() {
    val mockData = listOf(
        QuranListEntry(1, 1, 1, 1, 0, 0, 1, "J", "", "Al-Fatihah", "الفاتحة", ),
        QuranListEntry(8, 1, 2, 2, 0, 0, 1, "S", "", "Al-Baqarah", "البقرة")
    )
    MaterialTheme {
        QuranMenuScreen(mockData)
    }
}
