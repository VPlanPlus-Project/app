package plus.vplan.app.feature.search.domain.usecase

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.domain.model.SearchResult

class SearchUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {

    val lessons = mutableListOf<Lesson>()

    operator fun invoke(searchQuery: String, date: LocalDate) = channelFlow {
        if (searchQuery.isBlank()) return@channelFlow send(emptyMap())
        val query = searchQuery.lowercase().trim()
        val profile = getCurrentProfileUseCase().first()
        val school = profile.getSchoolItem()

        combine(
            groupRepository.getBySchool(school.id),
            teacherRepository.getBySchool(school.id),
            roomRepository.getBySchool(school.id)
        ) { groups, teachers, rooms ->
            groups
                .filter { query in it.name.lowercase() }
                .onEach { it.getSchoolItem() }
                .map { SearchResult.SchoolEntity.Group(it, emptyList()) } +
                    teachers
                        .filter { query in it.name.lowercase() }
                        .onEach { it.getSchoolItem() }
                        .map { SearchResult.SchoolEntity.Teacher(it, emptyList()) } +

                    rooms
                        .filter { query in it.name.lowercase() }
                        .onEach { it.getSchoolItem() }
                        .map { SearchResult.SchoolEntity.Room(it, emptyList()) }
        }.collectLatest { entityResult ->
            val send: suspend (results: List<SearchResult>) -> Unit = { send(it.groupBy { entityResult -> entityResult.type }) }
            send(entityResult)
            if (lessons.isEmpty()) lessons.addAll(substitutionPlanRepository.getSubstitutionPlanBySchool(school.id, date)
                .map { lessonIds -> lessonIds.map { lessonId -> App.substitutionPlanSource.getById(lessonId).getFirstValue() }.fastFilterNotNull().filter { it.subject != null }.onEach {
                    it.getLessonTimeItem()
                    it.getRoomItems()
                    it.getTeacherItems()
                } }
                .first())

            val result = entityResult.map { schoolEntity ->
                when (schoolEntity) {
                    is SearchResult.SchoolEntity.Group -> {
                        schoolEntity.copy(
                            lessons = lessons.filter { schoolEntity.group.id in it.groups }
                        )
                    }
                    is SearchResult.SchoolEntity.Teacher -> {
                        schoolEntity.copy(
                            lessons = lessons.filter { schoolEntity.teacher.id in it.teachers }
                        )
                    }
                    is SearchResult.SchoolEntity.Room -> {
                        schoolEntity.copy(
                            lessons = lessons.filter { schoolEntity.room.id in it.rooms.orEmpty() }
                        )
                    }
                }
            }
            send(result)
        }
    }
}