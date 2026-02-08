package plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import plus.vplan.app.ui.theme.displayFontFamily

@Composable
@Preview
fun OnboardingSchoolSearchHead(modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = "Lass uns deine Schule finden.",
            fontFamily = displayFontFamily(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = "Suche nach dem Namen oder der Stundenplan24.de-Schulnummer deiner Schule.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}