@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.search.domain.usecase

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.schulverwalter.Collection
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.calendar.ui.calculateLayouting
import plus.vplan.app.feature.search.domain.model.SearchResult
import plus.vplan.app.utils.now
import kotlin.uuid.ExperimentalUuidApi

class SearchUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val homeworkRepository: HomeworkRepository,
    private val assessmentRepository: AssessmentRepository
) {
    operator fun invoke(searchRequest: SearchRequest) = channelFlow {
        if (!searchRequest.hasActiveFilters) return@channelFlow send(emptyMap<SearchResult.Type, List<SearchResult>>())
        val query = searchRequest.query.lowercase().trim()
        val profile = getCurrentProfileUseCase().first()
        val school = profile.getSchool().getFirstValue() ?: return@channelFlow send(emptyMap<SearchResult.Type, List<SearchResult>>())

        launch {
            substitutionPlanRepository.getSubstitutionPlanBySchool(school.id, searchRequest.date).collectLatest { substitutionPlanLessonIds ->
                val lessons = substitutionPlanLessonIds
                    .map { id -> App.substitutionPlanSource.getById(id).getFirstValueOld() }
                    .fastFilterNotNull()
                    .filter { lesson -> lesson.subject != null }

                val results = MutableStateFlow(emptyMap<SearchResult.Type, List<SearchResult>>())
                launch { results.collect { send(it) } }

                if (searchRequest.query.isNotEmpty() && searchRequest.assessmentType == null) launch {
                    combine(
                        groupRepository.getBySchool(school.id).map { it.filter { group -> query in group.name.lowercase() } },
                        teacherRepository.getBySchool(school.id).map { it.filter { teacher -> query in teacher.name.lowercase() } },
                        roomRepository.getBySchool(school.id).map { it.filter { room -> query in room.name.lowercase() } },
                    ) { groups, teachers, rooms ->
                        results.value = results.value.plus(SearchResult.Type.Group to groups.map { group ->
                            group.getSchoolItem()
                            SearchResult.SchoolEntity.Group(
                                group = group,
                                lessons = lessons.filter { group.id in it.groupIds }.calculateLayouting()
                            )
                        })

                        results.value = results.value.plus(SearchResult.Type.Teacher to teachers.map { teacher ->
                            teacher.getSchoolItem()
                            SearchResult.SchoolEntity.Teacher(
                                teacher = teacher,
                                lessons = lessons.filter { teacher.id in it.teacherIds }.calculateLayouting()
                            )
                        })

                        results.value = results.value.plus(SearchResult.Type.Room to rooms.map { room ->
                            room.getSchoolItem()
                            SearchResult.SchoolEntity.Room(
                                room = room,
                                lessons = lessons.filter { room.id in it.roomIds }.calculateLayouting()
                            )
                        })
                    }.collect()
                }

                if (searchRequest.assessmentType == null) launch {
                    homeworkRepository.getAll().map { it.filterIsInstance<CacheStateOld.Done<Homework>>().map { item -> item.data } }.collectLatest { homeworkList ->
                        val homework = homeworkList.onEach { it.getTaskItems() }
                        results.value = results.value.plus(
                            SearchResult.Type.Homework to homework
                                .filter { (query.isEmpty() || it.taskItems!!.any { task -> query in task.content.lowercase() }) && (searchRequest.subject == null || it.subjectInstance?.getFirstValueOld()?.subject == searchRequest.subject) }
                                .onEach {
                                    it.subjectInstance?.getFirstValueOld() ?: it.group?.getFirstValue()
                                    when (it) {
                                        is Homework.CloudHomework -> it.getCreatedBy()
                                        is Homework.LocalHomework -> it.getCreatedByProfile()
                                    }
                                }
                                .map { SearchResult.Homework(it) })
                    }
                }

                launch {
                    assessmentRepository.getAll().collectLatest { assessmentList ->
                        val assessments = assessmentList
                            .filter { (query.isEmpty() || query in it.description.lowercase()) && (searchRequest.subject == null || it.subjectInstance.getFirstValueOld()?.subject == searchRequest.subject) && (searchRequest.assessmentType == null || it.type == searchRequest.assessmentType) }
                            .onEach { assessment ->
                                when (assessment.creator) {
                                    is AppEntity.VppId -> assessment.getCreatedByVppIdItem()
                                    is AppEntity.Profile -> assessment.getCreatedByProfileItem()
                                }
                            }
                            .sortedByDescending { (if (it.date < LocalDate.now()) "" else "_") + it.date.toString() }
                        results.value = results.value.plus(SearchResult.Type.Assessment to assessments.map { assessment -> SearchResult.Assessment(assessment) })
                    }
                }

                if (searchRequest.assessmentType == null) launch {
                    App.collectionSource.getAll().map { it.filterIsInstance<CacheStateOld.Done<Collection>>().map { it.data } }.collectLatest { collections ->
                        val filteredGrades = collections
                            .filter { query in it.name.lowercase() }
                            .flatMap { collection ->
                                collection.grades.first()
                                    .filter { grade -> grade.vppIdId == (profile as? Profile.StudentProfile)?.vppIdId }
                            }
                            .distinctBy { it.id }
                        results.value = results.value.plus(SearchResult.Type.Grade to filteredGrades.map { SearchResult.Grade(it) })
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