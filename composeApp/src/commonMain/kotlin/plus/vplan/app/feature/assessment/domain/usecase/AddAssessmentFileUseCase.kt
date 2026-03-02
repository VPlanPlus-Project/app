package plus.vplan.app.feature.assessment.domain.usecase

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock

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
                size = file.size(),
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
                size = file.size(),
                isOfflineReady = true,
                cachedAt = Clock.System.now()
            )
        )

        val fileItem = fileRepository.getById(id, forceReload = false).getFirstValueOld()!!
        val linkResult = assessmentRepository.linkFile(
            vppId = if (id > 0) profile.vppId?.asActive() else null,
            assessmentId = assessment.id,
            fileId = fileItem.id
        )
        return linkResult is Response.Success
    }

    private fun plus.vplan.app.core.model.VppId.asActive() = this as? plus.vplan.app.core.model.VppId.Active
}