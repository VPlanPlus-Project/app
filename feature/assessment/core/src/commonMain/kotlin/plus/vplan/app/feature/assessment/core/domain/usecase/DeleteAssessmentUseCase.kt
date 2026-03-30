package plus.vplan.app.feature.assessment.core.domain.usecase

import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Profile

class DeleteAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, profile: Profile.StudentProfile): Boolean {
        return assessmentRepository.deleteAssessment(assessment, profile) == null
    }
}