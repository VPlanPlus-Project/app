package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.SubjectInstanceRepository

class GetSubjectsForProfileUseCase: KoinComponent {
    private val subjectInstanceRepository by inject<SubjectInstanceRepository>()


    suspend operator fun invoke(profile: Profile): List<String> {
        val subjectInstances =
            if (profile is Profile.StudentProfile) subjectInstanceRepository.getByGroup(profile.group.id).first()
            else subjectInstanceRepository.getByTeacher(profile.school.id).first()

        return subjectInstances
            .map { it.subject }
            .sorted()
            .distinct()
    }
}