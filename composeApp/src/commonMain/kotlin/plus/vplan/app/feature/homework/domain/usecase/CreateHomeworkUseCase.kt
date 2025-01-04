package plus.vplan.app.feature.homework.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.feature.homework.ui.components.File

class CreateHomeworkUseCase(
    private val homeworkRepository: HomeworkRepository
) {
    suspend operator fun invoke(
        tasks: List<String>,
        isPublic: Boolean?,
        date: LocalDate,
        defaultLesson: DefaultLesson?,
        selectedFiles: List<File>
    ): Boolean {
//        val profile: Profile.StudentProfile = TODO()
//        val id: Int
//        val taskIds: Map<String, Int>
//        val homework: Homework
//        if (profile.vppId != null) {
//            val result = homeworkRepository.createHomeworkOnline(
//                vppId = profile.vppId,
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
//                createdBy = Cacheable.Loaded(profile.vppId),
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

        return true
    }
}
