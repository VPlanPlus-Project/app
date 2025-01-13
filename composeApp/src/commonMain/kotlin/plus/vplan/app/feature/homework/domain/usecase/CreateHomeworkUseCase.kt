package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.homework.ui.components.File

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
//        val profile: Profile.StudentProfile = run {
//            val configuration = Profile.Fetch(studentProfile = Profile.StudentProfile.Fetch(vppId = VppId.Fetch()))
//            keyValueRepository
//                .get(Keys.CURRENT_PROFILE)
//                .filterNotNull()
//                .first()
//                .let { App.profileSource.getById(it, configuration).first { it is Cacheable.Loaded && it.isConfigSatisfied(configuration, false) }.toValueOrNull() as Profile.StudentProfile }
//        }
//        val id: Int
//        val taskIds: Map<String, Int>
//        val homework: Homework
//        if (profile.vppId != null) {
//            val result = homeworkRepository.createHomeworkOnline(
//                vppId = profile.vppId.toValueOrNull()!!,
//                until = date,
//                group = profile.group.toValueOrNull()!!,
//                defaultLesson = defaultLesson,
//                isPublic = isPublic ?: false,
//                tasks = tasks
//            )
//            if (result !is Response.Success) return false
//
//            val idMapping = result.data
//            id = idMapping.id
//            taskIds = idMapping.taskIds
//            homework = Homework.CloudHomework(
//                id = id,
//                defaultLesson = defaultLesson?.let { Cacheable.Loaded(it) },
//                group = profile.group,
//                createdAt = Clock.System.now(),
//                createdBy = Cacheable.Loaded(profile.vppId.toValueOrNull()!!),
//                isPublic = isPublic ?: false,
//                dueTo = Instant.fromEpochSeconds(date.toEpochDays() * 24 * 60 * 60L),
//                tasks = taskIds.map { Cacheable.Loaded(Homework.HomeworkTask(id = it.value, content = it.key, homework = Cacheable.Uninitialized(id.toString()), isDone = false)) }
//            )
//        } else {
//            id = homeworkRepository.getIdForNewLocalHomework()
//            taskIds = tasks.associateWith { homeworkRepository.getIdForNewLocalHomeworkTask() }
//            homework = Homework.LocalHomework(
//                id = id,
//                defaultLesson = defaultLesson?.let { Cacheable.Loaded(it) },
//                createdAt = Clock.System.now(),
//                createdByProfile = profile.let { Cacheable.Loaded(it) },
//                dueTo = Instant.fromEpochSeconds(date.toEpochDays() * 24 * 60 * 60L),
//                tasks = taskIds.map { Cacheable.Loaded(Homework.HomeworkTask(id = it.value, content = it.key, homework = Cacheable.Uninitialized(id.toString()), isDone = false)) }
//            )
//        }
//
//        homeworkRepository.upsert(listOf(homework))
//
        return true
    }
}
