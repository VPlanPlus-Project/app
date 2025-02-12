package plus.vplan.app.feature.search.subfeature.room_search.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository

class GetRoomOccupationMapUseCase(
    private val roomRepository: RoomRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val weekRepository: WeekRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profile: Profile, date: LocalDate): Flow<Map<Room, Set<Occupancy>>> = channelFlow {
        val schoolId = profile.getSchool().first().entityId.toInt()
        combine(
            roomRepository.getBySchool(schoolId),
            timetableRepository.getTimetableForSchool(schoolId),
            substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId, date).flatMapLatest { ids -> if (ids.isEmpty()) flowOf(emptyList()) else combine(ids.map { App.substitutionPlanSource.getById(it) }) { it.toList().filterIsInstance<CacheState.Done<Lesson.SubstitutionPlanLesson>>().map { result -> result.data } } },
            weekRepository.getBySchool(schoolId)
        ) { rooms, timetable, substitution, weeks ->
            val currentWeek = weeks.firstOrNull { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date in it.start..it.end }
            substitution.hashCode()
            Pair(
                rooms.sortedBy { it.name },
                substitution
                    .ifEmpty { timetable.filter { it.dayOfWeek == date.dayOfWeek && (it.weekType == null || it.weekType == currentWeek?.weekType) } }
                    .onEach {
                        it.getLessonTimeItem()
                        it.getGroupItems()
                    }
            )
        }.collectLatest { (rooms, lessons) ->
            rooms.associateWith { room ->
                lessons.filter { room.id in it.rooms.orEmpty() }.map {
                    when (it) {
                        is Lesson.SubstitutionPlanLesson -> Occupancy.Lesson(it)
                        is Lesson.TimetableLesson -> Occupancy.Lesson(it, date)
                    }
                }.toSet()
            }.let { send(it) }
        }
    }
}

sealed class Occupancy(
    val start: LocalDateTime,
    val end: LocalDateTime
) {

    data class Lesson private constructor(val lesson: plus.vplan.app.domain.model.Lesson, val date: LocalDate) : Occupancy(lesson.lessonTimeItem!!.start.atDate(date), lesson.lessonTimeItem!!.end.atDate(date)) {
        constructor(lesson: plus.vplan.app.domain.model.Lesson.SubstitutionPlanLesson): this(lesson, lesson.date)
        constructor(lesson: plus.vplan.app.domain.model.Lesson.TimetableLesson, contextDate: LocalDate): this(lesson, date = contextDate)
    }
}