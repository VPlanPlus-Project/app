package plus.vplan.app.feature.home.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalTime

@Composable
fun Greeting(profileName: String, time: LocalTime) {
    Text(
        text = when (time.hour) {
            in 0..4 -> "Gute Nacht"
            in 5..9 -> "Guten Morgen"
            in 10..17 -> "Guten Tag"
            else -> "Guten Abend"
        },
        style = MaterialTheme.typography.headlineMedium
    )
    Text(
        text = profileName,
        style = MaterialTheme.typography.labelMedium
    )
}