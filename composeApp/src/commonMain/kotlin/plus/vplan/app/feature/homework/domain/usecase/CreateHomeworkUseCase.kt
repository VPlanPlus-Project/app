@file:OptIn(ExperimentalUuidApi::class, ExperimentalUuidApi::class)

package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.ui.common.AttachedFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val keyValueRepository: KeyValueRepository,
    private val localFileRepository: LocalFileRepository
) {
    suspend operator fun invoke(
        tasks: List<String>,
        isPublic: Boolean?,
        date: LocalDate,
        subjectInstance: SubjectInstance?,
        selectedFiles: List<AttachedFile>
    ): Int? {
        val profile = keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().first().let { App.profileSource.getById(Uuid.parseHex(it)).getFirstValue() as? Profile.StudentProfile } ?: return null
        val id: Int
        val taskIds: Map<String, Int>
        var homework: Homework
        val homeworkTasks: List<Homework.HomeworkTask>
        val files: List<Homework.HomeworkFile>
        if (profile.getVppIdItem() is VppId.Active) {
            val result = homeworkRepository.createHomeworkOnline(
                vppId = profile.getVppIdItem() as VppId.Active,
                until = date,
                group = profile.getGroupItem(),
                subjectInstance = subjectInstance,
                isPublic = isPublic == true,
                tasks = tasks
            )
            if (result !is Response.Success) return null

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

            files = selectedFiles.mapNotNull {
                val documentId = homeworkRepository.uploadHomeworkDocument(
                    vppId = profile.getVppIdItem() as VppId.Active,
                    homeworkId = homework.id,
                    document = it
                )
                if (documentId !is Response.Success) return@mapNotNull null
                homework = (homework as Homework.CloudHomework).copy(
                    files = homework.files + documentId.data
                )
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
            localFileRepository.writeFile("./homework_files/${file.id}", selectedFiles.first { it.name == file.name }.platformFile.readBytes())
        }

        return id
    }
}
