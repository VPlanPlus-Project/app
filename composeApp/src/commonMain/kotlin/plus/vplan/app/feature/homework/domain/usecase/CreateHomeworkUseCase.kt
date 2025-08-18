@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class)

package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import plus.vplan.app.captureError
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.service.ProfileService
import plus.vplan.app.domain.service.SubjectInstanceService
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.ExperimentalUuidApi

class CreateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val localFileRepository: LocalFileRepository,
    private val groupRepository: GroupRepository,
    private val profileService: ProfileService,
    private val subjectInstanceService: SubjectInstanceService,
) {
    suspend operator fun invoke(
        tasks: List<String>,
        isPublic: Boolean?,
        date: LocalDate,
        subjectInstance: SubjectInstance?,
        selectedFiles: List<AttachedFile>
    ): CreateHomeworkResult {
        val profile = (profileService.getCurrentProfile().first() as? Profile.StudentProfile) ?: return CreateHomeworkResult.Error.UnknownError("No current profile found or profile is not a student profile")
        val group = profile.group.getFirstValue() ?: return CreateHomeworkResult.Error.UnknownError("Group not found for profile ${profile.id} (${profile.vppIdId})")
        val id: Int
        val taskIds: Map<String, Int>
        var homework: Homework
        val homeworkTasks: List<Homework.HomeworkTask>
        val files: List<Homework.HomeworkFile>
        if (profile.getVppIdItem() is VppId.Active) {
            val vppGroupId = if (subjectInstance == null) {
                val groupId = group.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toIntOrNull() ?: run {
                    val groupAlias = group.aliases.firstOrNull()
                    if (groupAlias == null) return CreateHomeworkResult.Error.UnknownError("Group $group has no aliases")
                    val downloadedGroup = groupRepository.findByAlias(groupAlias, forceUpdate = true, preferCurrentState = true).getFirstValue()
                    if (downloadedGroup == null) return CreateHomeworkResult.Error.GroupNotFound
                    val groupId = downloadedGroup.aliases.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()
                    if (groupId == null) return CreateHomeworkResult.Error.UnknownError("Group $group not found on VPP")
                    return@run groupId
                }
                groupId
            } else {
                null
            }

            val vppSubjectInstanceId = if (subjectInstance != null) {
                val subjectInstanceId = subjectInstanceService.findAliasForSubjectInstance(subjectInstance, AliasProvider.Vpp)?.value?.toInt()
                if (subjectInstanceId == null) {
                    return CreateHomeworkResult.Error.UnknownError("Subject instance ${subjectInstance.id} not found on VPP")
                }
                subjectInstanceId
            } else {
                null
            }

            val result = homeworkRepository.createHomeworkOnline(
                vppId = profile.getVppIdItem() as VppId.Active,
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
                groupId = profile.groupId,
                createdAt = Clock.System.now(),
                createdBy = profile.vppIdId!!,
                isPublic = isPublic == true,
                dueTo = date,
                files = emptyList(),
                taskIds = taskIds.map { it.value },
                cachedAt = Clock.System.now()
            )
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homework = homework.id, doneByProfiles = emptyList(), doneByVppIds = emptyList(), cachedAt = Clock.System.now()) }

            files = emptyList()
//            selectedFiles.mapNotNull {
//                val documentId = homeworkRepository.uploadHomeworkDocument(
//                    vppId = profile.getVppIdItem() as VppId.Active,
//                    homeworkId = homework.id,
//                    document = it
//                )
//                if (documentId !is Response.Success) return@mapNotNull null
//                homework = (homework as Homework.CloudHomework).copy(
//                    files = homework.files + documentId.data
//                )
//                Homework.HomeworkFile(
//                    id = documentId.data,
//                    name = it.name,
//                    size = it.size,
//                    homework = homework.id
//                )
//            }
        } else {
            id = homeworkRepository.getIdForNewLocalHomework() - 1
            val taskIdStart = homeworkRepository.getIdForNewLocalHomeworkTask() - 1
            val fileIdStart = homeworkRepository.getIdForNewLocalHomeworkFile() - 1
            taskIds = tasks.mapIndexed { index, s -> s to (taskIdStart - index) }.toMap()
            files = selectedFiles.mapIndexed { index, file -> Homework.HomeworkFile(fileIdStart - index, file.name, id, file.size) }
            homework = Homework.LocalHomework(
                id = id,
                subjectInstanceId = subjectInstance?.id,
                createdAt = Clock.System.now(),
                createdByProfile = profile.id,
                dueTo = date,
                taskIds = taskIds.map { it.value },
                files = files.map { it.id },
                cachedAt = Clock.System.now()
            )
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homework = homework.id, doneByProfiles = emptyList(), doneByVppIds = emptyList(), cachedAt = Clock.System.now()) }
        }

        homeworkRepository.upsert(listOf(homework), homeworkTasks, files)
        homeworkRepository.createCacheForProfile(profile.id, setOf(homework.id))
        files.forEach { file ->
            localFileRepository.writeFile("./files/${file.id}", selectedFiles.first { it.name == file.name }.platformFile.readBytes())
        }

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