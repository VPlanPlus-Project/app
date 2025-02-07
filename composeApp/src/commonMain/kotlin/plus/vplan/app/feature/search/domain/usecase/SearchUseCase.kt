package plus.vplan.app.feature.search.domain.usecase

import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.search.domain.model.SearchResult

class SearchUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val getCurrentProfileUseCase: GetCurrentProfileUseCase
) {
    operator fun invoke(searchQuery: String) = channelFlow<List<SearchResult>> {
        val query = searchQuery.lowercase()
        val profile = getCurrentProfileUseCase().first()
        val school = profile.getSchoolItem()

        combine(
            groupRepository.getBySchool(school.id),
            teacherRepository.getBySchool(school.id),
            roomRepository.getBySchool(school.id)
        ) { groups, teachers, rooms ->
            groups
                .filter { query in it.name.lowercase() }
                .map { SearchResult.SchoolEntity.Group(it) }+

                    teachers
                        .filter { query in it.name.lowercase() }
                        .map { SearchResult.SchoolEntity.Teacher(it) } +

                    rooms
                        .filter { query in it.name.lowercase() }
                        .map { SearchResult.SchoolEntity.Room(it) }
        }.collectLatest {
            send(it)
        }
    }
}