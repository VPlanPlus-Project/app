package plus.vplan.app.feature.home.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import plus.vplan.app.feature.home.ui.HomeState
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.theme.AppTheme
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.key_round
import kotlin.uuid.Uuid

@Composable
fun Stundenplan24CredentialsExpiredCard(
    modifier: Modifier = Modifier,
    credentialsInvalidState: HomeState.Stundenplan24CredentialsInvalidState,
    onOpenSchoolSettings: () -> Unit
) {
    AnimatedVisibility(
        visible = credentialsInvalidState is HomeState.Stundenplan24CredentialsInvalidState.Invalid,
        enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
        modifier = modifier.fillMaxWidth(),
    ) {
        var stundenplan24CredentialsState by remember { mutableStateOf(credentialsInvalidState) }
        LaunchedEffect(credentialsInvalidState) {
            if (credentialsInvalidState !is HomeState.Stundenplan24CredentialsInvalidState.Invalid) return@LaunchedEffect
            stundenplan24CredentialsState = credentialsInvalidState
        }

        if (stundenplan24CredentialsState is HomeState.Stundenplan24CredentialsInvalidState.Invalid) InfoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            title = "Schulzugangsdaten abgelaufen",
            text = "Die Stundenplan24.de-Zugangsdaten für \"${(stundenplan24CredentialsState as HomeState.Stundenplan24CredentialsInvalidState.Invalid).schoolName}\" sind abgelaufen. Bitte aktualisiere sie, um weiterhin auf dem neuesten Stand zu bleiben.",
            color = MaterialTheme.colorScheme.error,
            buttonAction1 = onOpenSchoolSettings,
            buttonText1 = "Aktualisieren",
            imageVector = Res.drawable.key_round
        )
    }
}

@Preview
@Composable
private fun Stundenplan24CredentialsExpiredCardPreview() {
    AppTheme {
        Stundenplan24CredentialsExpiredCard(
            credentialsInvalidState = HomeState.Stundenplan24CredentialsInvalidState.Invalid(
                schoolName = "Musterschule",
                schoolId = Uuid.random()
            ),
            onOpenSchoolSettings = {}
        )
    }
}