package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult

class UpdateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(homeworkId: Int): UpdateResult {
        val homework = homeworkRepository.getById(homeworkId).first()
        if (homework == null) return UpdateResult.DOES_NOT_EXIST
        
        homework.files.forEach { file ->
            fileRepository.getById(file.id, forceReload = true).first()
        }
        
        return UpdateResult.SUCCESS
    }
}