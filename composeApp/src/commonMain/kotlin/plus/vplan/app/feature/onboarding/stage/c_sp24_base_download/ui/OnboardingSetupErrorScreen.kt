package plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataResult
import plus.vplan.app.feature.settings.page.info.ui.components.FeedbackDrawer
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.bug_play
import vplanplus.composeapp.generated.resources.undraw_warning

@Composable
fun OnboardingSetupErrorScreen(
    modifier: Modifier = Modifier,
    error: SetUpSchoolDataResult.Error
) {
    var isFeedbackDrawerVisible by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.undraw_warning),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp, top = 8.dp)
                .size(256.dp)
        )
        Text(
            text = "Sorry! Etwas ist schiefgelaufen.",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W600),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Wir konnten deine Schule nicht hinzufügen. Der Fehler wurde bereits an uns gesendet. Versuche es bitte später erneut.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { isFeedbackDrawerVisible = true },
        ) {
            Icon(
                painter = painterResource(Res.drawable.bug_play),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(16.dp)
            )
            Text("Fehler melden")
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Fehlerdetails",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }

    if (isFeedbackDrawerVisible) FeedbackDrawer(error.sp24Credentials) { isFeedbackDrawerVisible = false }
}

@Composable
@Preview
private fun OnboardingSetupErrorScreenPreview() {
    OnboardingSetupErrorScreen(
        error = SetUpSchoolDataResult.Error("Ein unbekannter Fehler ist aufgetreten.", VppSchoolAuthentication.Sp24("", "", ""))
    )
}