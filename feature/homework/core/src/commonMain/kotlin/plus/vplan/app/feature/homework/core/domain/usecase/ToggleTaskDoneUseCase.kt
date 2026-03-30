package plus.vplan.app.feature.homework.core.domain.usecase

import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile

class ToggleTaskDoneUseCase(
    private val homeworkRepository: HomeworkRepository
) {

    suspend operator fun invoke(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Boolean {
        return homeworkRepository.toggleTaskDone(task, profile)
    }
}