package plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.ui

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
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataState
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataStep
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.download

@Composable
fun OnboardingIndiwareDataDownloadScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingIndiwareDataDownloadViewModel>()
    val state = viewModel.state

    OnboardingIndiwareDataDownloadContent(
        state = state
    )

    LaunchedEffect(state.steps) {
        if (state.steps.values.all { it == SetUpSchoolDataState.DONE }) {
            delay(1000)
            navHostController.navigate(OnboardingScreen.OnboardingChooseProfile)
        }
    }
}

@Composable
private fun OnboardingIndiwareDataDownloadContent(
    state: OnboardingIndiwareDataDownloadUiState
) {
    AnimatedContent(state.error != null) { hasError ->
        if (hasError) {
            state.error?.let { OnboardingSetupErrorScreen(modifier = Modifier.safeDrawingPadding(), error = it) }
        } else {
            Column(
                modifier = Modifier
                    .safeDrawingPadding()
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
                        painter = painterResource(Res.drawable.download),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "VPlanPlus wird vorbereitet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Wir laden aktuelle Daten herunter, bitte warte einen kleinen Moment.",
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
                                    targetState = state
                                ) { displayState ->
                                    when (displayState) {
                                        SetUpSchoolDataState.IN_PROGRESS -> CircularProgressIndicator(
                                            Modifier.size(24.dp)
                                        )

                                        SetUpSchoolDataState.NOT_STARTED -> Box(
                                            modifier = Modifier.size(24.dp)
                                        )

                                        SetUpSchoolDataState.DONE -> Box(
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
                                        SetUpSchoolDataStep.SET_UP_DATA -> "Daten anpassen"
                                        SetUpSchoolDataStep.DOWNLOAD_BASE_DATA -> "Daten herunterladen"
                                        SetUpSchoolDataStep.GET_SCHOOL_INFORMATION -> "Schulinformationen laden"
                                        SetUpSchoolDataStep.GET_HOLIDAYS -> "Ferientage laden"
                                        SetUpSchoolDataStep.GET_GROUPS -> "Gruppen laden"
                                        SetUpSchoolDataStep.GET_TEACHERS -> "Lehrer laden"
                                        SetUpSchoolDataStep.GET_ROOMS -> "Räume laden"
                                        SetUpSchoolDataStep.GET_LESSON_TIMES -> "Stundenzeiten laden"
                                        SetUpSchoolDataStep.GET_WEEKS -> "Schulwochen laden"
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
    }
}