@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)

package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.captureError
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Homework
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkEntity
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.ui.common.AttachedFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

class CreateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val groupRepository: GroupRepository,
    private val fileRepository: FileRepository,
    private val localFileRepository: LocalFileRepository,
    private val profileService: ProfileService,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) {
    suspend operator fun invoke(
        tasks: List<String>,
        isPublic: Boolean?,
        date: LocalDate,
        subjectInstance: SubjectInstance?,
        selectedFiles: List<AttachedFile>
    ): CreateHomeworkResult {
        val profile = (profileService.getCurrentProfile().first() as? Profile.StudentProfile) ?: return CreateHomeworkResult.Error.UnknownError("No current profile found or profile is not a student profile")
        val id: Int
        val taskIds: Map<String, Int>
        var homework: Homework
        val homeworkTasks: List<Homework.HomeworkTask>
        val files: List<Homework.HomeworkFile>

        val vppGroupId = if (subjectInstance == null) {
            val groupId = profile.group.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
                val groupAlias = profile.group.aliases.firstOrNull()
                    ?: return CreateHomeworkResult.Error.UnknownError("Group ${profile.group} has no aliases")
                val downloadedGroup = groupRepository.findByAlias(
                    groupAlias,
                    forceUpdate = true,
                    preferCurrentState = true
                ).getFirstValue() ?: return CreateHomeworkResult.Error.GroupNotFound
                val groupId = downloadedGroup.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
                if (groupId == null) return CreateHomeworkResult.Error.UnknownError("Group ${profile.group} not found on VPP")
                return@run groupId
            }
            groupId
        } else {
            null
        }

        val vppSubjectInstanceId = if (subjectInstance != null) {
            val subjectInstanceId = subjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
                val subjectInstanceAlias = subjectInstance.aliases.firstOrNull()
                    ?: return CreateHomeworkResult.Error.UnknownError("Subject instance $subjectInstance has no aliases")
                val downloadedSubjectInstance = subjectInstanceRepository.findByAlias(
                    subjectInstanceAlias,
                    forceUpdate = true,
                    preferCurrentState = true
                ).getFirstValue()
                    ?: return CreateHomeworkResult.Error.UnknownError("Subject instance $subjectInstance not found on VPP")
                val subjectInstanceId = downloadedSubjectInstance.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
                if (subjectInstanceId == null) return CreateHomeworkResult.Error.UnknownError("Subject instance $subjectInstance not found on VPP")
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
            if (result !is Response.Success) return CreateHomeworkResult.Error.CreationError(result as Response.Error)

            val idMapping = result.data
            id = idMapping.id
            taskIds = idMapping.taskIds
            homework = Homework.CloudHomework(
                id = id,
                subjectInstanceId = subjectInstance?.id,
                groupId = if (subjectInstance == null) profile.group.id else null,
                createdAt = Clock.System.now(),
                createdById = profile.vppId!!.id,
                isPublic = isPublic == true,
                dueTo = date,
                fileIds = emptyList(),
                taskIds = taskIds.map { it.value },
                cachedAt = Clock.System.now()
            )
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homeworkId = homework.id, doneByProfiles = emptyList(), doneByVppIds = emptyList(), cachedAt = Clock.System.now()) }

            files = selectedFiles.mapNotNull {
                val documentId = fileRepository.uploadFile(
                    vppId = profile.vppId!!,
                    document = it
                )

                if (documentId !is Response.Success) return@mapNotNull null

                Homework.HomeworkFile(
                    id = documentId.data,
                    name = it.name,
                    size = it.size,
                    homework = homework.id
                )
            }
        } else {
            id = homeworkRepository.getIdForNewLocalHomework() - 1
            val taskIdStart = homeworkRepository.getIdForNewLocalHomeworkTask() - 1
            val fileIdStart = homeworkRepository.getIdForNewLocalHomeworkFile() - 1
            taskIds = tasks.mapIndexed { index, s -> s to (taskIdStart - index) }.toMap()
            files = selectedFiles.mapIndexed { index, file -> Homework.HomeworkFile(fileIdStart - index, file.name, id, file.size) }
            homework = Homework.LocalHomework(
                id = id,
                subjectInstanceId = subjectInstance?.id,
                groupId = if (subjectInstance == null) profile.group.id else null,
                createdAt = Clock.System.now(),
                createdByProfileId = profile.id,
                dueTo = date,
                taskIds = taskIds.map { it.value },
                fileIds = files.map { it.id },
                cachedAt = Clock.System.now()
            )
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homeworkId = homework.id, doneByProfiles = emptyList(), doneByVppIds = emptyList(), cachedAt = Clock.System.now()) }
        }

        files.forEach { file ->
            fileRepository.upsertLocally(
                fileId = file.id,
                fileName = file.name,
                fileSize = file.size,
                createdAt = Clock.System.now(),
                isOfflineReady = true,
                createdBy = if (homework.id > 0) profile.vppId?.id else null,
            )

            localFileRepository.writeFile("./files/${file.id}", selectedFiles.first { it.name == file.name }.platformFile.readBytes())
        }

        homeworkRepository.upsert(HomeworkEntity(
            id = homework.id,
            subjectInstanceId = homework.subjectInstanceId,
            groupId = if (subjectInstance == null) profile.group.id else null,
            dueTo = homework.dueTo,
            isPublic = isPublic ?: false,
            createdAt = Clock.System.now(),
            createdByVppId = if (homework.creator is AppEntity.VppId) homework.creator.id else null,
            createdByProfileId = if (homework.creator is AppEntity.Profile) homework.creator.id else null,
            cachedAt = Clock.System.now(),
            tasks = homeworkTasks.map { homeworkTask ->
                HomeworkEntity.TaskEntity(
                    id = homeworkTask.id,
                    homeworkId = homework.id,
                    createdAt = Clock.System.now(),
                    content = homeworkTask.content,
                    cachedAt = Clock.System.now()
                )
            }
        ))

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

        data class UnknownError(val message: String): Error() {
            init {
                captureError("CreateHomeworkUseCase", "Unknown error: $message")
            }
        }

        data class CreationError(val error: Response.Error) : Error()
    }
}