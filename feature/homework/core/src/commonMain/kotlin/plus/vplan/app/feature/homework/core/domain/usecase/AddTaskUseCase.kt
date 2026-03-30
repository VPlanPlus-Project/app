package plus.vplan.app.feature.homework.core.domain.usecase

import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile

class AddTaskUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, task: String, profile: Profile.StudentProfile): Boolean {
        return homeworkRepository.addTask(homework, task, profile) == null
    }
}