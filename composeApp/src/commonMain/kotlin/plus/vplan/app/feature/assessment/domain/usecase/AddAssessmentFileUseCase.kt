@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.assessment.domain.usecase

import io.github.vinceglb.filekit.core.PlatformFile
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AddAssessmentFileUseCase(
    private val localFileRepository: LocalFileRepository,
    private val fileRepository: FileRepository,
    private val assessmentRepository: AssessmentRepository
) {
    suspend operator fun invoke(assessment: Assessment, file: PlatformFile, profile: Profile.StudentProfile): Boolean {
        val id: Int
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active
        if (assessment.id > 0 && vppId != null) {
            val response = fileRepository.uploadFile(vppId, AttachedFile.Other(
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

        localFileRepository.writeFile("./files/$id", file.readBytes())
        fileRepository.upsert(
            File(
                id = id,
                name = file.name,
                size = file.getSize() ?: 0L,
                isOfflineReady = true,
                getBitmap = { null },
                cachedAt = Clock.System.now()
            )
        )

        val fileItem = fileRepository.getById(id, forceReload = false).getFirstValueOld()!!
        if (id > 0 && vppId != null) assessmentRepository.linkFileToAssessmentOnline(vppId, assessment.id, fileItem.id)
        assessmentRepository.linkFileToAssessment(assessment.id, fileItem.id)
        return true
    }
}