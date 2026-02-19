package app.quran4wearos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import app.quran4wearos.ui.theme.HafsRegularFontFamily
import kotlinx.coroutines.launch

class EntryPointsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Note: For production, inject this via Dagger/Hilt or use a custom Application class
        val repository = QuranRepository(applicationContext)

        setContent {
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
    TitleCard(
        onClick = onClick,
        title = {
            Text(
                text = if (entry.type == "S") entry.suraNameEn else "Juzz ${entry.jozz}",
                color = if (entry.type == "S") Color.Cyan else Color.Yellow,
                fontFamily = HafsRegularFontFamily
            )
        }
    ) {
        if (entry.type == "J") {
            Text(entry.text, style = MaterialTheme.typography.caption2)
        }
    }
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
