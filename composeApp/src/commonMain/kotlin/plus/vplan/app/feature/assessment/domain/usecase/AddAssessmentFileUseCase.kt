package plus.vplan.app.feature.assessment.domain.usecase

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.ui.common.AttachedFile

class AddAssessmentFileUseCase(
    private val localFileRepository: LocalFileRepository,
    private val fileRepository: FileRepository,
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, file: PlatformFile, profile: Profile.StudentProfile): Boolean {
        val id: Int
        if (assessment.id > 0 && profile.getVppIdItem() != null) {
            val response = fileRepository.uploadFile(profile.getVppIdItem()!!, AttachedFile.Other(
                platformFile = file,
                bitmap = null,
                size = file.getSize() ?: 0L,
                name = file.name,
            ))
            if (response !is Response.Success) return false
            id = response.data
        } else {
            id = fileRepository.getMinIdForLocalFile() - 1
        }

        localFileRepository.writeFile("./homework_files/$id", file.readBytes())
        fileRepository.upsert(plus.vplan.app.domain.model.File(
            id = id,
            name = file.name,
            size = file.getSize() ?: 0L,
            isOfflineReady = true,
            getBitmap = { null }
        ))

        val fileItem = fileRepository.getById(id, forceReload = false).filterIsInstance<CacheState.Done<plus.vplan.app.domain.model.File>>().first().data
        if (id > 0) assessmentRepository.linkFileToAssessmentOnline(profile.getVppIdItem()!!, assessment.id, fileItem.id)
        assessmentRepository.linkFileToAssessment(assessment.id, fileItem.id)
        return true
    }
}