package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class DeleteTaskUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val deleteHomeworkUseCase: DeleteHomeworkUseCase
) {
    suspend operator fun invoke(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Boolean {
        val homework = task.getHomeworkItem()
        if ((homework?.taskIds?.size ?: 0) <= 1) {
            return deleteHomeworkUseCase(homework!!, profile)
        }
        return homeworkRepository.deleteHomeworkTask(task, profile) == null
    }
}