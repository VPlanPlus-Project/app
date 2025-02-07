package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(homeworkId: Int) {
        homeworkRepository.getById(homeworkId, forceReload = true).first().also {
            if (it is CacheState.Done) {
                it.data.files.forEach { fileId ->
                    fileRepository.getById(fileId, forceReload = true)
                }
            }
        }
    }
}