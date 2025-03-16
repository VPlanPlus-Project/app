package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    displayName: String
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = "Hallo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.alignByBaseline()
        )
        Text(
            text = " $displayName 👋",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.alignByBaseline()
        )
    }
}