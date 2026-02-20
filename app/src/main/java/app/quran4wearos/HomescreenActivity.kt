package app.quran4wearos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import com.google.android.horologist.annotations.ExperimentalHorologistApi

class HomescreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    onSettingsClick: () -> Unit
) {
    // Use Wear-specific ScalingLazyColumn
    val scrollState: ScalingLazyListState = ScalingLazyListState()

//    android.util.Log.d("HomescreenActivity", "onCreate called")


    Scaffold(
        timeText = {
            // Optional: Add time text at the top
        },
//        modifier = Modifier.background(Color.White)
    ) {
        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                ListHeader {
                    androidx.wear.compose.material.Text(
                        "Quran for Wear OS",
                        style = MaterialTheme.typography.title2
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
                        text = "Quran",
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
                        text = "Settings",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}