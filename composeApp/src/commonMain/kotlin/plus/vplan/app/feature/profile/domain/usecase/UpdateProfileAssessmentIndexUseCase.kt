package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.populated.AssessmentPopulator
import plus.vplan.app.domain.model.populated.PopulatedAssessment
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.repository.AssessmentRepository

class UpdateProfileAssessmentIndexUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val assessmentPopulator: AssessmentPopulator
) {
    suspend operator fun invoke(profile: Profile) {
        assessmentRepository.dropIndicesForProfile(profile.id)
        assessmentRepository.getAll()
            .first()
            .let { assessmentPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }
            .filter { assessment ->
                val assessment = assessment
                (assessment is PopulatedAssessment.LocalAssessment && assessment.createdByProfile.id == profile.id) ||
                        (assessment is PopulatedAssessment.CloudAssessment && assessment.createdByUser.id == (profile as? Profile.StudentProfile)?.vppId?.id) ||
                        (assessment.subjectInstance.id in (profile as? Profile.StudentProfile)?.subjectInstanceConfiguration.orEmpty().filterValues { it }.keys && profile is Profile.StudentProfile)
            }
            .let { relevantAssessments ->
                assessmentRepository.createCacheForProfile(profile.id, relevantAssessments.map { it.assessment.id })
            }
    }
}
