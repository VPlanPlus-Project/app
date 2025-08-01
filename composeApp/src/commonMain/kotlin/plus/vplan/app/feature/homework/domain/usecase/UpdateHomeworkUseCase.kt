package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(homeworkId: Int): UpdateResult {
        return when (homeworkRepository.getById(homeworkId, forceReload = true).first().also {
            if (it is CacheStateOld.Done) {
                it.data.files.forEach { fileId ->
                    fileRepository.getById(fileId, forceReload = true)
                }
            }
        }) {
            is CacheStateOld.Done -> UpdateResult.SUCCESS
            is CacheStateOld.NotExisting -> UpdateResult.DOES_NOT_EXIST
            else -> UpdateResult.ERROR
        }
    }
}