package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class EditHomeworkDefaultLessonUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, defaultLesson: DefaultLesson?, profile: Profile.StudentProfile) {
        homeworkRepository.editHomeworkDefaultLesson(homework, defaultLesson, if (defaultLesson == null) profile.getGroupItem() else null, profile)
    }
}