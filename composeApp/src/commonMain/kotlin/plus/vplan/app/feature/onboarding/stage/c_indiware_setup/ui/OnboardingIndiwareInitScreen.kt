package plus.vplan.app.feature.onboarding.stage.c_indiware_setup.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepState
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepType
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.server_cog

@Composable
fun OnboardingIndiwareInitScreen(
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<OnboardingIndiwareInitViewModel>()
    val state = viewModel.state

    LaunchedEffect(state.steps.all { it.value == IndiwareInitStepState.SUCCESS }) {
        if (state.steps.all { it.value == IndiwareInitStepState.SUCCESS }) {
            delay(1000)
            navHostController.navigate(OnboardingScreen.OnboardingIndiwareDataDownload)
        }
    }

    OnboardingIndiwareInitContent(state)
}

@Composable
private fun OnboardingIndiwareInitContent(
    state: OnboardingIndiwareInitState,
) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(bottom = 16.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.server_cog),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Schule wird eingerichtet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Du bist der erste Nutzer dieser Schule. Wir müssen noch ein paar Dinge vorbereiten.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                state.steps.forEach { (stage, state) ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = state,
                        ) { displayState ->
                            when (displayState) {
                                IndiwareInitStepState.IN_PROGRESS -> CircularProgressIndicator(Modifier.size(24.dp))
                                IndiwareInitStepState.NOT_STARTED -> Box(modifier = Modifier.size(24.dp))
                                IndiwareInitStepState.SUCCESS -> Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = when (stage) {
                                IndiwareInitStepType.DATA_LOADED -> "Daten laden"
                                IndiwareInitStepType.DATA_UPDATED -> "Daten aktualisieren"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}