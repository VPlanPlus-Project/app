package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class UpdateTaskUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(task: Homework.HomeworkTask, newContent: String, profile: Profile.StudentProfile) {
        homeworkRepository.editHomeworkTask(task, newContent, profile)
    }
}