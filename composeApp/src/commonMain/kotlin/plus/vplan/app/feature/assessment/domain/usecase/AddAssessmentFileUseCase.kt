@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.assessment.domain.usecase

import io.github.vinceglb.filekit.core.PlatformFile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Profile
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
        if (assessment.id > 0 && profile.vppId != null) {
            val response = fileRepository.uploadFile(profile.vppId!!, AttachedFile.Other(
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
                cachedAt = Clock.System.now()
            )
        )

        val fileItem = fileRepository.getById(id, forceReload = false).getFirstValueOld()!!
        if (id > 0 && profile.vppId != null) assessmentRepository.linkFileToAssessmentOnline(profile.vppId!!, assessment.id, fileItem.id)
        assessmentRepository.linkFileToAssessment(assessment.id, fileItem.id)
        return true
    }
}