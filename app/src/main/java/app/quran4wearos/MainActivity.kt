package app.quran4wearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TitleCard
import app.quran4wearos.ui.theme.HafsRegularFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parser = QuranFileParser(this)

        setContent {
            var quranData by remember { mutableStateOf<List<QuranEntry>>(emptyList()) }

            // Load data on startup
            LaunchedEffect(Unit) {
                quranData = withContext(Dispatchers.IO) {
                    parser.loadData("quran_metadata.csv")
                }
            }

            MaterialTheme() {
                if (quranData.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    QuranViewerApp(quranData)
                }
            }
        }
    }
}

@Composable
fun QuranViewerApp(items: List<QuranEntry>) {
    var selectedAyaKey by remember { mutableStateOf<Int?>(null) }

    if (selectedAyaKey == null) {
        val listState = rememberScalingLazyListState()
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
                    QuranItem(entry) {
                        selectedAyaKey = (entry.suraNo * 1000) + entry.ayaNo
                    }
                }
            }
        }
    } else {
        ConfirmationScreen(ayaKey = selectedAyaKey!!) { selectedAyaKey = null }
    }
}

@Composable
fun QuranItem(entry: QuranEntry, onClick: () -> Unit) {
    TitleCard(
        onClick = onClick,
        title = {
            Text(
                text = if (entry.type == "S") entry.suraName else "Juzz ${entry.jozz}",
                color = if (entry.type == "S") Color.Cyan else Color.Yellow,
                fontFamily = HafsRegularFontFamily)
        }
    ) {
        if (entry.type == "J" && entry.text.isNotEmpty()) {
            Text(entry.text, style = MaterialTheme.typography.caption2)
        }
    }
}

@Composable
fun ConfirmationScreen(ayaKey: Int, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Selected ID", style = MaterialTheme.typography.caption1)
        Text(ayaKey.toString(), style = MaterialTheme.typography.title1, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onDismiss) { Text("OK") }
    }
}