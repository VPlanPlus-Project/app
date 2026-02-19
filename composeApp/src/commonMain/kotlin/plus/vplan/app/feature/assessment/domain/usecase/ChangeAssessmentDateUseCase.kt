package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.datetime.LocalDate
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository

class ChangeAssessmentDateUseCase(
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, date: LocalDate, profile: Profile.StudentProfile) {
        assessmentRepository.changeDate(assessment, date, profile)
    }
}