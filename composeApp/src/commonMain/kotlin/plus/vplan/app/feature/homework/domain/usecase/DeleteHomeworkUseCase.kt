package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class DeleteHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, profile: Profile.StudentProfile): Boolean {
        return homeworkRepository.deleteHomework(homework, profile) == null
    }
}