package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class AddTaskUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, task: String, profile: Profile.StudentProfile): Boolean {
        return homeworkRepository.addTask(homework, task, profile) == null
    }
}