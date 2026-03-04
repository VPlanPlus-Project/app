package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.Profile

class GetSubjectsForProfileUseCase: KoinComponent {
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()


    suspend operator fun invoke(profile: Profile): List<String> {
        val subjectInstances = when (profile) {
            is Profile.StudentProfile -> subjectInstanceRepository.getByGroup(profile.group).first()
            is Profile.TeacherProfile -> subjectInstanceRepository.getByTeacher(profile.teacher).first()
            else -> throw IllegalStateException("Profile cannot be in this state")
        }

        return subjectInstances
            .map { it.subject }
            .sorted()
            .distinct()
    }
}