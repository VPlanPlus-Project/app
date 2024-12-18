package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolData

class OnboardingIndiwareDataDownloadViewModel(
    private val setUpSchoolData: SetUpSchoolData
) : ViewModel() {

    var state by mutableStateOf(OnboardingIndiwareDataDownloadUiState())
        private set

    init {
        viewModelScope.launch {
            val result = setUpSchoolData()
            state = state.copy(success = result)
        }
    }
}

data class OnboardingIndiwareDataDownloadUiState(
    val success: Boolean? = null
)