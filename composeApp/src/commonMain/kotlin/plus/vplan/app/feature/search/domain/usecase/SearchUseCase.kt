package plus.vplan.app.feature.search.domain.usecase

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.domain.model.Result
import plus.vplan.app.feature.search.domain.model.SearchResult

class SearchUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val homeworkRepository: HomeworkRepository,
    private val assessmentRepository: AssessmentRepository
) {

    private val lessons = mutableListOf<Lesson>()
    private var lessonDate: LocalDate? = null

    operator fun invoke(searchQuery: String, date: LocalDate) = channelFlow {
        if (searchQuery.isBlank()) return@channelFlow send(emptyMap<Result, List<SearchResult>>())
        val query = searchQuery.lowercase().trim()
        val profile = getCurrentProfileUseCase().first()
        val school = profile.getSchoolItem()

        if (lessons.isEmpty() || lessonDate != date) {
            lessons.clear()
            lessons.addAll(substitutionPlanRepository
                .getSubstitutionPlanBySchool(school.id, date).map {
                    it
                        .map { id -> App.substitutionPlanSource.getById(id).getFirstValue() }
                        .fastFilterNotNull()
                        .filter { lesson -> lesson.subject != null }
                        .onEach { lesson ->
                            lesson.getLessonTimeItem()
                            lesson.getTeacherItems()
                            lesson.getRoomItems()
                        }
                }.first()
            )
        }

        val results = MutableStateFlow(emptyMap<Result, List<SearchResult>>())
        launch { results.collect { send(it) } }

        launch {
            combine(
                groupRepository.getBySchool(school.id).map { it.filter { group -> query in group.name.lowercase() } },
                teacherRepository.getBySchool(school.id).map { it.filter { teacher -> query in teacher.name.lowercase() } },
                roomRepository.getBySchool(school.id).map { it.filter { room -> query in room.name.lowercase() } },
            ) { groups, teachers, rooms ->
                results.value = results.value.plus(Result.Group to groups.map { group ->
                    group.getSchoolItem()
                    SearchResult.SchoolEntity.Group(
                        group = group,
                        lessons = lessons.filter { group.id in it.groups }
                    )
                })

                results.value = results.value.plus(Result.Teacher to teachers.map { teacher ->
                    teacher.getSchoolItem()
                    SearchResult.SchoolEntity.Teacher(
                        teacher = teacher,
                        lessons = lessons.filter { teacher.id in it.teachers }
                    )
                })

                results.value = results.value.plus(Result.Room to rooms.map { room ->
                    room.getSchoolItem()
                    SearchResult.SchoolEntity.Room(
                        room = room,
                        lessons = lessons.filter { room.id in it.rooms.orEmpty() }
                    )
                })
            }.collect()
        }

        launch {
            homeworkRepository.getAll().map { it.filterIsInstance<CacheState.Done<Homework>>().map { item -> item.data } }.collectLatest { homeworkList ->
                val homework = homeworkList.onEach { it.getTaskItems() }
                results.value = results.value.plus(Result.Homework to homework.filter { it.taskItems!!.any { task -> query in task.content.lowercase() } }.onEach {
                    it.getDefaultLessonItem() ?: it.getGroupItem()
                    when (it) {
                        is Homework.CloudHomework -> it.getCreatedBy()
                        is Homework.LocalHomework -> it.getCreatedByProfile()
                    }
                }.map { SearchResult.Homework(it) })
            }
        }

        launch {
            assessmentRepository.getAll().collectLatest { assessmentList ->
                val assessments = assessmentList.filter { query in it.description.lowercase() }
                    .onEach { assessment ->
                        assessment.getSubjectInstanceItem()
                        when (assessment.creator) {
                            is AppEntity.VppId -> assessment.getCreatedByVppIdItem()
                            is AppEntity.Profile -> assessment.getCreatedByProfileItem()
                        }
                    }
                results.value = results.value.plus(Result.Assessment to assessments.map { assessment -> SearchResult.Assessment(assessment) })
            }
        }
    }
}