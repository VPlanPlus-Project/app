package plus.vplan.app.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.onboarding.domain.usecase.InitialiseOnboardingWithSchoolIdUseCase

class OnboardingHostViewModel(
    private val initialiseOnboardingWithSchoolIdUseCase: InitialiseOnboardingWithSchoolIdUseCase,
    private val schoolRepository: SchoolRepository,
) : ViewModel() {
    fun init(schoolIdentifier: Set<Alias>?) {
        viewModelScope.launch {
            if (schoolIdentifier != null && schoolIdentifier.isNotEmpty()) {
                val school = schoolRepository.getByIds(schoolIdentifier).first() as? School.AppSchool
                    ?: return@launch
                initialiseOnboardingWithSchoolIdUseCase(school)
            }
        }
    }
}