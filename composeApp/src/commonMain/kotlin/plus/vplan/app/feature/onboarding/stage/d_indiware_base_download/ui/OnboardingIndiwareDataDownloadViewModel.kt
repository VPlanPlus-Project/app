package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolData

class OnboardingIndiwareDataDownloadViewModel(
    private val setUpSchoolData: SetUpSchoolData
) : ViewModel() {

    init {
        viewModelScope.launch {
            setUpSchoolData()
        }
    }
}