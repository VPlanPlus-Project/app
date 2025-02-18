package plus.vplan.app.feature.sync.domain.usecase.vpp

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.ProfileRepository

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke() {
        profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>().forEach { profile ->
            assessmentRepository.download(profile.getVppIdItem()?.buildSchoolApiAccess() ?: profile.getSchoolItem().getSchoolApiAccess()!!, profile.defaultLessons.filterValues { it }.keys.toList())
        }
    }
}