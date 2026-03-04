package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile

class DeleteTaskUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val deleteHomeworkUseCase: DeleteHomeworkUseCase
) {
    suspend operator fun invoke(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Boolean {
        val homework = homeworkRepository.getById(task.homeworkId).first() ?: return false
        if (homework.tasks.size <= 1) {
            return deleteHomeworkUseCase(homework, profile)
        }
        return homeworkRepository.deleteTask(task, profile) == null
    }
}