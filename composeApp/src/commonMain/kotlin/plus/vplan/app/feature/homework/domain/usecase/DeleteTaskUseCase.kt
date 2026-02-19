package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class DeleteTaskUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val deleteHomeworkUseCase: DeleteHomeworkUseCase
) {
    suspend operator fun invoke(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Boolean {
        val homework = homeworkRepository.getByLocalId(task.homeworkId).first() ?: return false
        if (homework.taskIds.size <= 1) {
            return deleteHomeworkUseCase(homework, profile)
        }
        return homeworkRepository.deleteHomeworkTask(task, profile) == null
    }
}