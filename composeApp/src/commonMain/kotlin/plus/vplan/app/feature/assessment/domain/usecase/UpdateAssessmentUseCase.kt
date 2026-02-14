package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.FileRepository

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(id: Int): UpdateResult {
        return when(assessmentRepository.getById(id, true).first { it !is CacheState.Loading }.also {
            if (it is CacheState.Done) {
                it.data.fileIds.map { fileId ->
                    fileRepository.getById(fileId, true).first { it !is CacheState.Loading }
                }
            }
        }) {
            is CacheState.Done -> UpdateResult.SUCCESS
            is CacheState.NotExisting -> UpdateResult.DOES_NOT_EXIST
            else -> UpdateResult.ERROR
        }
    }
}

enum class UpdateResult {
    SUCCESS, ERROR, DOES_NOT_EXIST
}