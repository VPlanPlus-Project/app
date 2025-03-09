package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class EditHomeworkSubjectInstanceUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, subjectInstance: SubjectInstance?, profile: Profile.StudentProfile) {
        homeworkRepository.editHomeworkSubjectInstance(homework, subjectInstance, if (subjectInstance == null) profile.getGroupItem() else null, profile)
    }
}