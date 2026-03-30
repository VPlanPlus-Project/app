package plus.vplan.app.feature.homework.core.domain.usecase

import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Optional
import plus.vplan.app.core.model.Profile

class EditHomeworkVisibilityUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile) {
        homeworkRepository.updateHomeworkMetadata(homework, isPublic = Optional.of(isPublic), profile = profile)
    }
}