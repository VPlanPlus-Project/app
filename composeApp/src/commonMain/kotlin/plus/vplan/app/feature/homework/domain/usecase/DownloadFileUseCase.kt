package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import plus.vplan.app.data.repository.FileDownloadProgress
import plus.vplan.app.domain.model.File
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository

class DownloadFileUseCase(
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository
) {
    operator fun invoke(file: File, profile: Profile.StudentProfile): Flow<Float> =
        fileRepository.downloadFileContent(file, profile.vppId?.buildVppSchoolAuthentication(-1) ?: profile.school.buildSp24AppAuthentication()).onEach {
            if (it is FileDownloadProgress.Done) {
                localFileRepository.writeFile("./files/${file.id}", it.content)
                it.onFileSaved()
            }
        }
            .filterIsInstance<FileDownloadProgress.InProgress>()
            .map { it.progress }
}