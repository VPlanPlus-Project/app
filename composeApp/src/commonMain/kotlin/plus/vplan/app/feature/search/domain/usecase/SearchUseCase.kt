@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.getFirstValue
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.populated.HomeworkPopulator
import plus.vplan.app.domain.model.populated.LessonPopulator
import plus.vplan.app.domain.model.populated.PopulationContext
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.calendar.ui.calculateLayouting
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.utils.now

class SearchUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val homeworkRepository: HomeworkRepository,
    private val homeworkPopulator: HomeworkPopulator,
    private val assessmentRepository: AssessmentRepository
): KoinComponent {
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val lessonPopulator by inject<LessonPopulator>()

    operator fun invoke(searchRequest: SearchRequest) = channelFlow {
        if (!searchRequest.hasActiveFilters) return@channelFlow send(emptyMap())
        val query = searchRequest.query.lowercase().trim()
        val profile = getCurrentProfileUseCase().first()

        launch {
            substitutionPlanRepository.getSubstitutionPlanBySchool(profile.school.id, searchRequest.date).collectLatest { substitutionPlanLessons ->
                val lessons = substitutionPlanLessons
                    .let { lessonPopulator.populateMultiple(it, PopulationContext.Profile(profile)).first() }
                    .filter { lesson -> lesson.lesson.subject != null }

                val results = MutableStateFlow(emptyMap<SearchResult.Type, List<SearchResult>>())
                launch { results.collect { send(it) } }

                if (searchRequest.query.isNotEmpty() && searchRequest.assessmentType == null) launch {
                    combine(
                        groupRepository.getBySchool(profile.school.id).map { it.filter { group -> query in group.name.lowercase() } },
                        teacherRepository.getBySchool(profile.school.id).map { it.filter { teacher -> query in teacher.name.lowercase() } },
                        roomRepository.getBySchool(profile.school.id).map { it.filter { room -> query in room.name.lowercase() } },
                    ) { groups, teachers, rooms ->
                        results.value = results.value.plus(SearchResult.Type.Group to groups.map { group ->
                            SearchResult.SchoolEntity.Group(
                                group = group,
                                lessons = lessons.filter { group.id in it.lesson.groupIds }.calculateLayouting()
                            )
                        })

                        results.value = results.value.plus(SearchResult.Type.Teacher to teachers.map { teacher ->
                            SearchResult.SchoolEntity.Teacher(
                                teacher = teacher,
                                lessons = lessons.filter { teacher.id in it.lesson.teacherIds }.calculateLayouting()
                            )
                        })

                        results.value = results.value.plus(SearchResult.Type.Room to rooms.map { room ->
                            SearchResult.SchoolEntity.Room(
                                room = room,
                                lessons = lessons.filter { room.id in it.lesson.roomIds.orEmpty() }.calculateLayouting()
                            )
                        })
                    }.collect()
                }

                if (searchRequest.assessmentType == null) launch {
                    homeworkRepository.getAll()
                        .flatMapLatest { homeworkPopulator.populateMultiple(it, PopulationContext.Profile(profile)) }
                        .collectLatest { homework ->
                            results.value = results.value.plus(
                                SearchResult.Type.Homework to homework
                                    .filter { (query.isEmpty() || it.tasks.any { task -> query in task.content.lowercase() }) && (searchRequest.subject == null || it.subjectInstance?.subject == searchRequest.subject) }
                                    .map { SearchResult.Homework(it) })
                        }
                }

                launch {
                    assessmentRepository.getAll().collectLatest { assessmentList ->
                        val assessments = assessmentList
                            .filter { (query.isEmpty() || query in it.description.lowercase()) && (searchRequest.subject == null || it.subjectInstance.getFirstValue()?.subject == searchRequest.subject) && (searchRequest.assessmentType == null || it.type == searchRequest.assessmentType) }
                            .sortedByDescending { (if (it.date < LocalDate.now()) "" else "_") + it.date.toString() }
                        results.value = results.value.plus(SearchResult.Type.Assessment to assessments.map { assessment -> SearchResult.Assessment(assessment) })
                    }
                }

                if (searchRequest.assessmentType == null) launch {
                    besteSchuleGradesRepository.getGrades(
                        responsePreference = ResponsePreference.Fast,
                        contextBesteschuleAccessToken = null,
                        contextBesteschuleUserId = null
                    )
                        .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
                        .map { response ->
                            response.data.filter { grade -> query.lowercase() in grade.collection.first()!!.name }
                        }
                        .collectLatest { grades ->
                            results.value = results.value.plus(SearchResult.Type.Grade to grades.map { SearchResult.Grade(it) })
                        }
                }
            }
        }
    }
}

data class SearchRequest(
    val query: String = "",
    val date: LocalDate = LocalDate.now(),
    val subject: String? = null,
    val assessmentType: Assessment.Type? = null
) {
    val hasActiveFilters = query.isNotEmpty() || subject != null
}