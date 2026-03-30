package plus.vplan.app.feature.homework.core.domain.usecase

import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile

class DeleteHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, profile: Profile.StudentProfile): Boolean {
        return homeworkRepository.deleteHomework(homework, profile) == null
    }
}