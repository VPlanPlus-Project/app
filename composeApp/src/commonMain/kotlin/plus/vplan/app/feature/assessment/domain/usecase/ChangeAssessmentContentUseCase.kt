package plus.vplan.app.feature.assessment.domain.usecase

import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class ChangeAssessmentContentUseCase(
    private val assessmentRepository: AssessmentRepository
) {

    suspend operator fun invoke(assessment: Assessment, content: String, profile: Profile.StudentProfile) {
        assessmentRepository.changeContent(assessment, profile, content)
    }
}