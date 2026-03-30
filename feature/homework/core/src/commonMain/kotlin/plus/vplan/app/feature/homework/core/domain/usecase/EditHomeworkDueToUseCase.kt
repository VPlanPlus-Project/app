package plus.vplan.app.feature.homework.core.domain.usecase

import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile

class EditHomeworkDueToUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile) {
        homeworkRepository.updateHomeworkMetadata(homework, dueTo = Optional.of(dueTo), profile = profile)
    }
}