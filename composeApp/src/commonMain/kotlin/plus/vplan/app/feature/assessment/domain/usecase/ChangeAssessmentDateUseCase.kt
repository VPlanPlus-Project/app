package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile

class ChangeAssessmentDateUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, date: LocalDate, profile: Profile.StudentProfile) {
        assessmentRepository.updateAssessmentMetadata(assessment, date = Optional.of(date), profile = profile)
    }
}