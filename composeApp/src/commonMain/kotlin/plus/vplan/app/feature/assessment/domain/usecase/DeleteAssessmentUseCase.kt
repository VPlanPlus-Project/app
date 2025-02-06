package plus.vplan.app.feature.assessment.domain.usecase

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class DeleteAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, profile: Profile.StudentProfile): Boolean {
        return assessmentRepository.deleteAssessment(assessment, profile) == null
    }
}