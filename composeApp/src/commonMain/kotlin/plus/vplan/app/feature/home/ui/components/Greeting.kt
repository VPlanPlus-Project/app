package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    displayName: String,
    time: LocalTime
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when (time.hour) {
                in 0..4 -> "Gute Nacht"
                in 5..9 -> "Guten Morgen"
                in 10..17 -> "Guten Tag"
                else -> "Guten Abend"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.alignByBaseline()
        )
        Text(
            text = displayName,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.alignByBaseline()
        )
    }
}