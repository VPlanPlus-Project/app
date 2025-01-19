package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class EditHomeworkVisibilityUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile) {
        homeworkRepository.editHomeworkVisibility(homework, isPublic, profile)
    }
}