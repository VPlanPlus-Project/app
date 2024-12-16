package plus.vplan.app.feature.onboarding.stage.c_indiware_setup.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepState
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepType
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.usecase.TrackIndiwareProgressUseCase

class OnboardingIndiwareInitViewModel(
    private val trackIndiwareProgressUseCase: TrackIndiwareProgressUseCase
) : ViewModel() {
    var state by mutableStateOf(OnboardingIndiwareInitState())
        private set

    init {
        viewModelScope.launch {
            trackIndiwareProgressUseCase().collect { response ->
                if (response is Response.Success) {
                    state = OnboardingIndiwareInitState(response.data)
                }
            }
        }
    }
}

data class OnboardingIndiwareInitState(
    val steps: Map<IndiwareInitStepType, IndiwareInitStepState> = IndiwareInitStepType.entries.associateWith { IndiwareInitStepState.NOT_STARTED }
)