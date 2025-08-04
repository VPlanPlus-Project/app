package plus.vplan.app.feature.onboarding.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import kotlin.uuid.Uuid

class InitialiseOnboardingWithSchoolIdUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val schoolRepository: SchoolRepository
) {
    suspend operator fun invoke(schoolId: Uuid?) {
        val school = schoolId?.let { schoolRepository.getByLocalId(it).first() as? School.AppSchool }
        onboardingRepository.reset()
        if (school != null) {
            onboardingRepository.startSp24Onboarding(school.sp24Id.toInt())
        }
    }
}