package plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataResult
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataState
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataStep
import plus.vplan.app.feature.onboarding.stage.c_sp24_base_download.domain.usecase.SetUpSchoolDataUseCase

class OnboardingIndiwareDataDownloadViewModel(
    private val setUpSchoolDataUseCase: SetUpSchoolDataUseCase
) : ViewModel() {

    var state by mutableStateOf(OnboardingIndiwareDataDownloadUiState())
        private set

    init {
        viewModelScope.launch {
            setUpSchoolDataUseCase().collect { result ->
                state = when (result) {
                    is SetUpSchoolDataResult.Loading -> OnboardingIndiwareDataDownloadUiState(result.data)
                    is SetUpSchoolDataResult.Error -> OnboardingIndiwareDataDownloadUiState(error = result)
                }
            }
        }
    }
}

data class OnboardingIndiwareDataDownloadUiState(
    val steps: Map<SetUpSchoolDataStep, SetUpSchoolDataState> = SetUpSchoolDataStep.entries.associateWith { SetUpSchoolDataState.NOT_STARTED },
    val error: SetUpSchoolDataResult.Error? = null
)