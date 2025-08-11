package plus.vplan.app.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import plus.vplan.app.feature.onboarding.domain.usecase.InitialiseOnboardingWithSchoolIdUseCase
import kotlin.uuid.Uuid

class OnboardingHostViewModel(
    private val initialiseOnboardingWithSchoolIdUseCase: InitialiseOnboardingWithSchoolIdUseCase
) : ViewModel() {
    fun init(schoolId: Uuid?) {
        viewModelScope.launch {
            initialiseOnboardingWithSchoolIdUseCase(schoolId)
        }
    }
}