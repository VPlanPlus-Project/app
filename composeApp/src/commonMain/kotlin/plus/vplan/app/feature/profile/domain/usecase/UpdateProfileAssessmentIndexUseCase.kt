package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class UpdateProfileAssessmentIndexUseCase(
    private val assessmentRepository: AssessmentRepository,
) {
    suspend operator fun invoke(profile: Profile) {
        assessmentRepository.dropIndicesForProfile(profile.id)
        assessmentRepository.getAll()
            .first()
            .filter { assessment ->
                val assessment = assessment
                (assessment.creator is AppEntity.Profile && (assessment.creator as AppEntity.Profile).profile.id == profile.id) ||
                        (assessment.creator is AppEntity.VppId && (assessment.creator as AppEntity.VppId).vppId.id == (profile as? Profile.StudentProfile)?.vppId?.id) ||
                        (assessment.subjectInstance.id in (profile as? Profile.StudentProfile)?.subjectInstanceConfiguration.orEmpty().filterValues { it }.keys.map { it.id } && profile is Profile.StudentProfile)
            }
            .let { relevantAssessments ->
                assessmentRepository.createCacheForProfile(profile.id, relevantAssessments.map { it.id })
            }
    }
}
