package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.repository.HomeworkRepository

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homeworkId: Int) {
        homeworkRepository.getById(homeworkId, forceReload = true).first()
    }
}