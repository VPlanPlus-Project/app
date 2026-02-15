package plus.vplan.app.feature.assessment.domain.usecase

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class ChangeAssessmentVisibilityUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, isPublic: Boolean, profile: Profile.StudentProfile) {
        assessmentRepository.changeVisibility(assessment, isPublic, profile)
    }
}