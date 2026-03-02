package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.populated.AssessmentPopulator
import plus.vplan.app.domain.repository.AssessmentRepository

class UpdateProfileAssessmentIndexUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val assessmentPopulator: AssessmentPopulator
) {
    suspend operator fun invoke(profile: Profile) {
        assessmentRepository.dropIndicesForProfile(profile.id)
        assessmentRepository.getAll()
            .first()
            .let { assessmentPopulator.populateMultiple(it).first() }
            .filter { assessment ->
                val assessment = assessment
                (assessment.assessment.creator is AppEntity.Profile && (assessment.assessment.creator as AppEntity.Profile).profile.id == profile.id) ||
                        (assessment.assessment.creator is AppEntity.VppId && (assessment.assessment.creator as AppEntity.VppId).vppId.id == (profile as? Profile.StudentProfile)?.vppId?.id) ||
                        (assessment.assessment.subjectInstance.id in (profile as? Profile.StudentProfile)?.subjectInstanceConfiguration.orEmpty().filterValues { it }.keys.map { it.id } && profile is Profile.StudentProfile)
            }
            .let { relevantAssessments ->
                assessmentRepository.createCacheForProfile(profile.id, relevantAssessments.map { it.assessment.id })
            }
    }
}
