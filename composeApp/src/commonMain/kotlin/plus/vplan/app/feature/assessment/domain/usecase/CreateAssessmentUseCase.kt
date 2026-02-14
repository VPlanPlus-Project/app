package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.File
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock

class CreateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository,
    private val profileService: ProfileService,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) {
    suspend operator fun invoke(
        text: String,
        isPublic: Boolean?,
        date: LocalDate,
        subjectInstance: SubjectInstance,
        type: Assessment.Type,
        selectedFiles: List<AttachedFile>
    ): CreateAssessmentResult {
        val profile = (profileService.getCurrentProfile().first() as? Profile.StudentProfile) ?: return CreateAssessmentResult.Error.UnknownError("No current profile found or profile is not a student profile")

        val creator = profile.vppId?.let { vppId ->
            AppEntity.VppId(vppId.id)
        } ?: AppEntity.Profile(profile.id)

        val subjectInstanceId = subjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
            val subjectInstanceAlias = subjectInstance.aliases.firstOrNull()
            if (subjectInstanceAlias == null) return CreateAssessmentResult.Error.UnknownError("Subject instance $subjectInstance has no aliases")
            val downloadedSubjectInstance = subjectInstanceRepository.findByAlias(subjectInstanceAlias, forceUpdate = true, preferCurrentState = true).getFirstValue()
            if (downloadedSubjectInstance == null) return CreateAssessmentResult.Error.UnknownError("Subject instance $subjectInstance not found on VPP")
            val subjectInstanceId = downloadedSubjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
            if (subjectInstanceId == null) return CreateAssessmentResult.Error.UnknownError("Subject instance $subjectInstance not found on VPP")
            return@run subjectInstanceId
        }
        subjectInstanceId

        val id: Int
        val files = mutableListOf<Assessment.AssessmentFile>()
        if (profile.vppId != null) {
            val result = assessmentRepository.createAssessmentOnline(
                vppId = profile.vppId,
                date = date,
                type = type,
                subjectInstanceId = subjectInstanceId,
                isPublic = isPublic ?: false,
                content = text
            )
            if (result !is Response.Success) return CreateAssessmentResult.Error.CreationError(result as Response.Error)
            id = result.data
            selectedFiles.forEach {
                val fileId = fileRepository.uploadFile(
                    vppId = profile.vppId,
                    document = it
                )
                if (fileId !is Response.Success) return@forEach
                assessmentRepository.linkFileToAssessmentOnline(
                    vppId = profile.vppId,
                    assessmentId = result.data,
                    fileId = fileId.data
                )
                files.add(Assessment.AssessmentFile(
                    id = fileId.data,
                    name = it.name,
                    size = it.size,
                    assessment = result.data
                ))
            }
        } else {
            id = assessmentRepository.getIdForNewLocalAssessment() - 1
            files.addAll(selectedFiles.mapIndexed { index, attachedFile ->
                Assessment.AssessmentFile(
                    id = fileRepository.getMinIdForLocalFile()-1-index,
                    name = attachedFile.name,
                    assessment = id,
                    size = attachedFile.size
                )
            })
        }

        files.forEach { file ->
            localFileRepository.writeFile("./files/${file.id}", selectedFiles.first { it.name == file.name }.platformFile.readBytes())
            fileRepository.upsert(File(
                name = file.name,
                id = file.id,
                isOfflineReady = true,
                size = file.size,
                getBitmap = { null },
                cachedAt = Clock.System.now()
            ))
        }
        assessmentRepository.upsertLocally(
            assessmentId = id,
            subjectInstanceId = subjectInstanceId,
            date = date,
            isPublic = isPublic,
            createdAt = Clock.System.now(),
            createdBy = if (creator is AppEntity.VppId) creator.id else null,
            createdByProfile = if (creator is AppEntity.Profile) creator.id else null,
            description = text,
            type = type,
            associatedFileIds = files.map { it.id }
        )
        assessmentRepository.createCacheForProfile(profile.id, setOf(id))
        return CreateAssessmentResult.Success(id)
    }
}

sealed class CreateAssessmentResult {
    data class Success(val assessmentId: Int) : CreateAssessmentResult()
    sealed class Error : CreateAssessmentResult() {
        data class UnknownError(val message: String): Error()
        data class CreationError(val error: Response.Error) : Error()
    }
}