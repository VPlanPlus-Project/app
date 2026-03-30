package plus.vplan.app.feature.assessment.core.domain.usecase

import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile

class ChangeAssessmentVisibilityUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, isPublic: Boolean, profile: Profile.StudentProfile) {
        assessmentRepository.updateAssessmentMetadata(assessment, isPublic = Optional.of(isPublic), profile = profile)
    }
}