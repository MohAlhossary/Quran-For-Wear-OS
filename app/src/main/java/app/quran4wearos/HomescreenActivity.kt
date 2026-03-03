package app.quran4wearos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import com.google.android.horologist.annotations.ExperimentalHorologistApi

class HomescreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize SettingsManager
        SettingsManager.initialize(applicationContext)

        setContent {
            HomeScreen(
                onQuranClick = {
                    // Navigate to Quran screen/activity
                    startActivity(Intent(this, EntryPointsActivity::class.java))
                },
                onSettingsClick = {
                    // Navigate to Settings screen/activity
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalHorologistApi::class)
@Composable
fun HomeScreen(
    onQuranClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val scrollState: ScalingLazyListState = rememberScalingLazyListState()
    val context = LocalContext.current

    Scaffold(
        timeText = {
            // Optional: Add time text at the top
        }
    ) {
        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ListHeader {
                    androidx.wear.compose.material.Text(
                        getStringInLanguage(R.string.homescreen_title, SettingsManager.settings.collectAsState().value.language),
                        style = MaterialTheme.typography.title2,
                    )
                }
            }

            item {
                Card(
                    onClick = onQuranClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        text = if (settings.language == "AR") "القرآن" else "Quran",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Card(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        text = if (settings.language == "AR") "الإعدادات" else "Settings",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

// Simple ViewModel for HomeScreen to access settings
class HomeScreenViewModel : ViewModel() {
    val settings = SettingsManager.settings
}