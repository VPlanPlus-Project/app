package plus.vplan.app.feature.onboarding.stage.c_indiware_setup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepState

@Composable
fun OnboardingIndiwareInitScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingIndiwareInitViewModel>()
    val state = viewModel.state

    OnboardingIndiwareInitContent(state)
}

@Composable
private fun OnboardingIndiwareInitContent(
    state: OnboardingIndiwareInitState
) {
    Column {
        state.steps.forEach { (type, state) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (state) {
                    IndiwareInitStepState.IN_PROGRESS -> CircularProgressIndicator(Modifier.size(24.dp))
                    IndiwareInitStepState.NOT_STARTED -> Box(modifier = Modifier.size(24.dp))
                    IndiwareInitStepState.SUCCESS -> Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("D")
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(type.name)
            }
        }
    }
}