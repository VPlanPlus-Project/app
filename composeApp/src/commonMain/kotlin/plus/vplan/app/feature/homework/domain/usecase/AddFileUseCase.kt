@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.homework.domain.usecase

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AddFileUseCase(
    private val localFileRepository: LocalFileRepository,
    private val fileRepository: FileRepository,
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(homework: Homework, file: PlatformFile, profile: Profile.StudentProfile): Boolean {
        val fileId: Int
        if (homework.id > 0 && profile.getVppIdItem() != null) {
            val fileUploadResponse = fileRepository.uploadFile(
                vppId = profile.vppId!!.getFirstValueOld() as VppId.Active,
                document = AttachedFile.fromFile(file)
            )
            if (fileUploadResponse !is Response.Success) return false
            val response = homeworkRepository.linkHomeworkFile(
                vppId = profile.vppId!!.getFirstValueOld() as VppId.Active,
                homeworkId = homework.id,
                fileId = fileUploadResponse.data
            )
            if (response !is Response.Success) return false
            fileId = fileUploadResponse.data
        } else {
            fileId = homeworkRepository.getIdForNewLocalHomeworkFile() - 1
            fileRepository.upsert(plus.vplan.app.domain.model.File(
                id = fileId,
                name = file.name,
                size = file.getSize() ?: 0L,
                isOfflineReady = true,
                getBitmap = { null },
                cachedAt = Clock.System.now()
            ))
            homeworkRepository.linkHomeworkFile(
                vppId = null,
                homeworkId = homework.id,
                fileId = fileId
            )
        }

        localFileRepository.writeFile("./files/$fileId", file.readBytes())
        val fileItem = fileRepository.getById(fileId, false).filterIsInstance<CacheState.Done<File>>().first().data
        fileRepository.setOfflineReady(fileItem, true)
        return true
    }
}