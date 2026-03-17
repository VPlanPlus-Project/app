package plus.vplan.app.feature.homework.domain.usecase

import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.file.FileRepository
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbFile
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.File
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.application.network.ApiException
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.domain.usecase.file.UploadFileUseCase
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock

class CreateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val groupRepository: GroupRepository,
    private val uploadFileUseCase: UploadFileUseCase,
    private val vppDatabase: VppDatabase,
    private val fileRepository: FileRepository,
    private val profileService: ProfileService,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend operator fun invoke(
        tasks: List<String>,
        isPublic: Boolean?,
        date: LocalDate,
        subjectInstance: SubjectInstance?,
        selectedFiles: List<AttachedFile>
    ): CreateHomeworkResult {
        val profile = (profileService.getCurrentProfile().first() as? Profile.StudentProfile) ?: return unknownError("No current profile found or profile is not a student profile")
        val id: Int
        val taskIds: Map<String, Int>
        var homework: Homework
        val homeworkTasks: List<Homework.HomeworkTask>
        val files: List<Homework.HomeworkFile>

        val vppGroupId = if (subjectInstance == null) {
            val groupId = profile.group.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
                val groupAlias = profile.group.aliases.firstOrNull()
                    ?: return unknownError("Group ${profile.group} has no aliases")
                val downloadedGroup = try {
                    groupRepository.getById(
                        identifier = groupAlias,
                        forceUpdate = true,
                    ).first() ?: return CreateHomeworkResult.Error.GroupNotFound
                } catch (e: ApiException) {
                    return CreateHomeworkResult.Error.UnknownError(e.stackTraceToString())
                }
                val groupId = downloadedGroup.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
                if (groupId == null) return unknownError("Group ${profile.group} not found on VPP")
                return@run groupId
            }
            groupId
        } else {
            null
        }

        val vppSubjectInstanceId = if (subjectInstance != null) {
            val subjectInstanceId = subjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
                val subjectInstanceAlias = subjectInstance.aliases.firstOrNull()
                    ?: return unknownError("Subject instance $subjectInstance has no aliases")
                val downloadedSubjectInstance = subjectInstanceRepository.getById(subjectInstanceAlias)
                    .first()
                    ?: return unknownError("Subject instance $subjectInstance not found on VPP")
                val subjectInstanceId = downloadedSubjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
                if (subjectInstanceId == null) return unknownError("Subject instance $subjectInstance not found on VPP")
                return@run subjectInstanceId
            }
            subjectInstanceId
        } else {
            null
        }

        if (profile.vppId is VppId.Active) {
            val result = homeworkRepository.createHomeworkOnline(
                vppId = profile.vppId!!,
                until = date,
                groupId = vppGroupId,
                subjectInstanceId = vppSubjectInstanceId,
                isPublic = isPublic == true,
                tasks = tasks
            )
            if (result !is Response.Success) {
                val error = result as Response.Error
                analyticsRepository.captureError("CreateHomeworkUseCase", "Creation error: ${error::class.simpleName}")
                return CreateHomeworkResult.Error.CreationError(error)
            }

            val idMapping = result.data
            id = idMapping.id
            taskIds = idMapping.taskIds
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homeworkId = id, doneByProfiles = emptyList(), doneByVppIds = emptyList(), cachedAt = Clock.System.now()) }
            homework = Homework.CloudHomework(
                id = id,
                subjectInstance = subjectInstance,
                group = if (subjectInstance == null) profile.group else null,
                createdAt = Clock.System.now(),
                createdBy = profile.vppId!!,
                isPublic = isPublic == true,
                dueTo = date,
                files = emptyList(),
                tasks = homeworkTasks,
                cachedAt = Clock.System.now()
            )

            files = selectedFiles.mapNotNull {
                // Upload file using the new use case
                val uploadResult = uploadFileUseCase(profile.vppId!!, it.platformFile)
                
                if (uploadResult !is Response.Success) return@mapNotNull null
                
                val uploadedFile = uploadResult.data

                Homework.HomeworkFile(
                    id = uploadedFile.id,
                    name = uploadedFile.name,
                    size = uploadedFile.size,
                    homework = homework.id
                )
            }
        } else {
            // Local homework (no VppId.Active)
            id = homeworkRepository.getIdForNewLocalHomework() - 1
            val taskIdStart = homeworkRepository.getIdForNewLocalHomeworkTask() - 1
            val fileIdStart = homeworkRepository.getIdForNewLocalHomeworkFile() - 1
            taskIds = tasks.mapIndexed { index, s -> s to (taskIdStart - index) }.toMap()
            
            // For local homework, create File objects manually
            val localFiles = selectedFiles.mapIndexed { index, attachedFile ->
                File(
                    id = fileIdStart - index,
                    name = attachedFile.name,
                    size = attachedFile.size,
                    isOfflineReady = true,
                    cachedAt = Clock.System.now()
                )
            }
            
            files = localFiles.map { file ->
                Homework.HomeworkFile(
                    id = file.id,
                    name = file.name,
                    size = file.size,
                    homework = id
                )
            }
            
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homeworkId = id, doneByProfiles = emptyList(), doneByVppIds = emptyList(), cachedAt = Clock.System.now()) }
            homework = Homework.LocalHomework(
                id = id,
                subjectInstance = subjectInstance,
                group = if (subjectInstance == null) profile.group else null,
                createdAt = Clock.System.now(),
                createdByProfile = profile,
                dueTo = date,
                tasks = homeworkTasks,
                files = localFiles,
                cachedAt = Clock.System.now()
            )
        }

        homeworkRepository.save(homework)

        // Write local files for local homework
        if (profile.vppId !is VppId.Active) {
            files.forEach { file ->
                vppDatabase.fileDao.upsert(DbFile(
                    id = file.id,
                    fileName = file.name,
                    createdAt = Clock.System.now(),
                    createdByVppId = null,
                    size = file.size,
                    isOfflineReady = true,
                    cachedAt = Clock.System.now(),
                    thumbnailPath = null,
                    mimeType = null
                ))

                fileRepository.writeLocalFile(file.id, selectedFiles.first { it.name == file.name }.platformFile.readBytes())
            }
        }

        files.forEach { file ->
            homeworkRepository.linkHomeworkFile(
                vppId = if (homework.id > 0) profile.vppId else null,
                homeworkId = homework.id,
                fileId = file.id
            )
        }
        homeworkRepository.createCacheForProfile(profile.id, setOf(homework.id))

        return CreateHomeworkResult.Success(id)
    }

    private fun unknownError(message: String): CreateHomeworkResult.Error.UnknownError {
        analyticsRepository.captureError("CreateHomeworkUseCase", "Unknown error: $message")
        return CreateHomeworkResult.Error.UnknownError(message)
    }
}

sealed class CreateHomeworkResult {
    data class Success(val homeworkId: Int) : CreateHomeworkResult()
    sealed class Error : CreateHomeworkResult() {

        /**
         * A numeric vpp id for the group could not be found. This can be the case if the group is
         * not linked to a VPP entity because it does not exist on the VPP server, or if the user
         * has no internet connection whilst the group is not linked to a VPP entity.
         */
        data object GroupNotFound : Error()

        data class UnknownError(val message: String): Error()

        data class CreationError(val error: Response.Error) : Error()
    }
}