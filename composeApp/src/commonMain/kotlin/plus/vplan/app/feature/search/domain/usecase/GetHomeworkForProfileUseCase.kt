package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile

class GetHomeworkForProfileUseCase(
    private val homeworkRepository: HomeworkRepository,
) {
    operator fun invoke(profile: Profile.StudentProfile): Flow<List<Homework>> {
        return homeworkRepository.getByProfile(profile.id)
    }
}