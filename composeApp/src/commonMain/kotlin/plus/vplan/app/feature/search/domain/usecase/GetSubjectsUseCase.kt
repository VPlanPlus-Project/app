package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.SubjectInstanceRepository

class GetSubjectsUseCase(
    private val subjectInstanceRepository: SubjectInstanceRepository
) {
    suspend operator fun invoke(profile: Profile): List<String> {
        return (if (profile is Profile.StudentProfile) profile.subjectInstances.first()
        else subjectInstanceRepository.getBySchool(profile.school.id).first()).map { it.subject }.sorted().distinct()
    }
}