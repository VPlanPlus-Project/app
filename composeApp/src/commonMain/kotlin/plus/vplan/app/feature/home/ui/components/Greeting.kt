package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import plus.vplan.app.ui.theme.displayFontFamily

@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    displayName: String?
) {
    Row(
        modifier = modifier
    ) {
        Text(
            text = buildString {
                append("Hallo")
                if (displayName != null) append(",")
            },
            fontFamily = displayFontFamily(),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = " ${displayName?.plus("!").orEmpty()} 👋",
            fontFamily = displayFontFamily(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Preview
@Composable
fun GreetingPreview() {
    Greeting(displayName = "Max Mustermann")
}