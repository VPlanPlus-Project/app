package plus.vplan.app.feature.onboarding.stage.school_select.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.theme.displayFontFamily

@Composable
@Preview
fun OnboardingSchoolSearchHead(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = "Finde deine Schule",
            fontFamily = displayFontFamily(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = "Suche nach dem Namen oder der Stundenplan24.de-Schulnummer deiner Schule.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )
    }
}