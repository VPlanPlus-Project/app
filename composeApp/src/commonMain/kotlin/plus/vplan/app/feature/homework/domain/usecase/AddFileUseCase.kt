package plus.vplan.app.feature.homework.domain.usecase

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.ui.common.AttachedFile

class AddFileUseCase(
    private val localFileRepository: LocalFileRepository,
    private val fileRepository: FileRepository,
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, file: PlatformFile, profile: Profile.StudentProfile): Boolean {
        val id: Int
        if (homework.id > 0 && profile.getVppIdItem() != null) {
            val response = homeworkRepository.uploadHomeworkDocument(profile.getVppIdItem()!!, homework.id, AttachedFile.Other(
                platformFile = file,
                bitmap = null,
                size = file.getSize() ?: 0L,
                name = file.name,
            ))
            if (response !is Response.Success) return false
            id = response.data
        } else {
            id = homeworkRepository.getIdForNewLocalHomeworkFile() - 1
            fileRepository.upsert(plus.vplan.app.domain.model.File(
                id = id,
                name = file.name,
                size = file.getSize() ?: 0L,
                isOfflineReady = true,
                getBitmap = { null },
                cachedAt = Clock.System.now()
            ))
        }

        localFileRepository.writeFile("./homework_files/$id", file.readBytes())
        val fileItem = fileRepository.getById(id, false).filterIsInstance<CacheState.Done<plus.vplan.app.domain.model.File>>().first().data
        fileRepository.setOfflineReady(fileItem, true)
        homeworkRepository.linkHomeworkFileLocally(homework, fileItem)
        return true
    }
}