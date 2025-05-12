package plus.vplan.app.feature.onboarding.stage.migrate.a_read.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase.MigrationDataReadResult
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.smartphone

@Composable
fun ImportScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<ImportScreenViewModel>()

    LaunchedEffect(viewModel.state.isDone) {
        if (viewModel.state.isDone) navHostController.navigate(OnboardingScreen.OnboardingPermission)
    }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.smartphone),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Übertrage deine Daten",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            AnimatedContent(
                targetState = viewModel.state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { isLoading ->
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Wir richten jetzt VPlanPlus mit deinen alten Einstellungen ein. Das dauert ein wenig, bitte lasse die App geöffnet. Es müssen noch einige Daten heruntergeladen werden.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        CircularProgressIndicator()
                    }
                }
                else Text(
                    text = "Füge hier den kopierten Text aus der alten App ein. Sobald der korrekte Text eingefügt wurde, geht's weiter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = viewModel.state.result
            ) { error ->
                Text(
                    text = when (error) {
                        MigrationDataReadResult.InvalidSequence -> "Stelle sicher, dass du die neue App aus der alten heraus geöffnet hast."
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}