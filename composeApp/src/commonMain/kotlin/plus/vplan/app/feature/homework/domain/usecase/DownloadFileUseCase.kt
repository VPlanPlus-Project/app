package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import plus.vplan.app.data.repository.FileDownloadProgress
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DownloadFileUseCase(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(file: File, profile: Profile.StudentProfile): Flow<Float> =
        fileRepository.cacheFile(file, profile.getVppIdItem()?.buildSchoolApiAccess(-1) ?: profile.getSchool().getFirstValue()!!.getSchoolApiAccess()).onEach {
            if (it is FileDownloadProgress.Done) {
                localFileRepository.writeFile("./homework_files/${file.id}", it.content)
                it.onFileSaved()
            }
        }
            .filterIsInstance<FileDownloadProgress.InProgress>()
            .map { it.progress }
}