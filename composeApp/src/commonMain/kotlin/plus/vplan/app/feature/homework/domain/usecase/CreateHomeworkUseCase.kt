package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.homework.ui.components.File
import kotlin.uuid.Uuid

class CreateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(
        tasks: List<String>,
        isPublic: Boolean?,
        date: LocalDate,
        defaultLesson: DefaultLesson?,
        selectedFiles: List<File>
    ): Boolean {
        val profile = keyValueRepository.get(Keys.CURRENT_PROFILE).filterNotNull().first().let { App.profileSource.getById(Uuid.parseHex(it)).getFirstValue() as? Profile.StudentProfile } ?: return false
        val id: Int
        val taskIds: Map<String, Int>
        val homework: Homework
        val homeworkTasks: List<Homework.HomeworkTask>
        if (profile.getVppIdItem() is VppId.Active) {
            val result = homeworkRepository.createHomeworkOnline(
                vppId = profile.getVppIdItem() as VppId.Active,
                until = date,
                group = profile.getGroupItem(),
                defaultLesson = defaultLesson,
                isPublic = isPublic ?: false,
                tasks = tasks
            )
            if (result !is Response.Success) return false

            val idMapping = result.data
            id = idMapping.id
            taskIds = idMapping.taskIds
            homework = Homework.CloudHomework(
                id = id,
                defaultLesson = defaultLesson?.getEntityId(),
                group = profile.group,
                createdAt = Clock.System.now(),
                createdBy = profile.vppId!!,
                isPublic = isPublic ?: false,
                dueTo = Instant.fromEpochSeconds(date.toEpochDays() * 24 * 60 * 60L),
                tasks = taskIds.map { it.value }
            )
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homework = homework.id, isDone = null) }
        } else {
            id = homeworkRepository.getIdForNewLocalHomework()
            taskIds = tasks.associateWith { homeworkRepository.getIdForNewLocalHomeworkTask() }
            homework = Homework.LocalHomework(
                id = id,
                defaultLesson = defaultLesson?.id,
                createdAt = Clock.System.now(),
                createdByProfile = profile.id,
                dueTo = Instant.fromEpochSeconds(date.toEpochDays() * 24 * 60 * 60L),
                tasks = taskIds.map { it.value }
            )
            homeworkTasks = taskIds.map { Homework.HomeworkTask(id = it.value, content = it.key, homework = homework.id, isDone = null) }
        }

        homeworkRepository.upsert(listOf(homework), homeworkTasks)

        return true
    }
}
