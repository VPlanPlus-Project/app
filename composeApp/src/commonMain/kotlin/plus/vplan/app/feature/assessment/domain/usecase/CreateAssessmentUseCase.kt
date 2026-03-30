package plus.vplan.app.feature.assessment.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.feature.file.core.domain.model.AttachedFile
import plus.vplan.app.feature.file.core.domain.usecase.UploadFileUseCase

class CreateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val uploadFileUseCase: UploadFileUseCase,
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

        val subjectInstanceId = subjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
            val subjectInstanceAlias = subjectInstance.aliases.firstOrNull()
                ?: return CreateAssessmentResult.Error.UnknownError("Subject instance $subjectInstance has no aliases")
            val downloadedSubjectInstance = subjectInstanceRepository.getById(
                subjectInstanceAlias,
            ).first()
                ?: return CreateAssessmentResult.Error.UnknownError("Subject instance $subjectInstance not found on VPP")
            val subjectInstanceId = downloadedSubjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
            if (subjectInstanceId == null) return CreateAssessmentResult.Error.UnknownError("Subject instance $subjectInstance not found on VPP")
            return@run subjectInstanceId
        }

        val activeVppId = profile.vppId
            ?: return CreateAssessmentResult.Error.UnknownError("Profile has no active VPP ID")

        val result = assessmentRepository.createAssessmentOnline(
            vppId = activeVppId,
            date = date,
            type = type,
            subjectInstanceId = subjectInstanceId,
            isPublic = isPublic ?: false,
            content = text
        )
        
        if (result !is Response.Success) {
            return CreateAssessmentResult.Error.CreationError(result as Response.Error)
        }
        
        val assessmentId = result.data

        val files = mutableListOf<Assessment.AssessmentFile>()
        selectedFiles.forEach { attachedFile ->
            // Upload file using the new use case
            val uploadResult = uploadFileUseCase(activeVppId, attachedFile.platformFile)
            if (uploadResult !is Response.Success) {
                // Skip files that failed to upload
                return@forEach
            }
            
            val uploadedFile = uploadResult.data
            
            // Link the uploaded file to the assessment
            assessmentRepository.linkFile(
                vppId = activeVppId,
                assessmentId = assessmentId,
                fileId = uploadedFile.id
            )
            
            files.add(Assessment.AssessmentFile(
                id = uploadedFile.id,
                name = uploadedFile.name,
                size = uploadedFile.size,
                assessment = assessmentId
            ))
        }

        return CreateAssessmentResult.Success(assessmentId)
    }
}

sealed class CreateAssessmentResult {
    data class Success(val assessmentId: Int) : CreateAssessmentResult()
    sealed class Error : CreateAssessmentResult() {
        data class UnknownError(val message: String): Error()
        data class CreationError(val error: Response.Error) : Error()
    }
}