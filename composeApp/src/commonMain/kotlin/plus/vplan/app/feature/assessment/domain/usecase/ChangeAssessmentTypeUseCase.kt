package plus.vplan.app.feature.assessment.domain.usecase

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class ChangeAssessmentTypeUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, type: Assessment.Type, profile: Profile.StudentProfile) {
        assessmentRepository.changeType(assessment, type, profile)
    }
}