package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class EditHomeworkDueToUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile) {
        homeworkRepository.editHomeworkDueTo(homework, dueTo, profile)
    }
}