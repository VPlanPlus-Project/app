package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository

class GetHomeworkForProfileUseCase(
    private val homeworkRepository: HomeworkRepository,
) {
    operator fun invoke(profile: Profile.StudentProfile): Flow<List<Homework>> {
        return homeworkRepository.getByProfile(profile.id)
    }
}