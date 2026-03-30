package plus.vplan.app.feature.assessment.core.domain.usecase

import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile

class ChangeAssessmentContentUseCase(
    private val assessmentRepository: AssessmentRepository
) {

    suspend operator fun invoke(assessment: Assessment, content: String, profile: Profile.StudentProfile) {
        assessmentRepository.updateAssessmentMetadata(assessment, content = Optional.of(content), profile = profile)
    }
}