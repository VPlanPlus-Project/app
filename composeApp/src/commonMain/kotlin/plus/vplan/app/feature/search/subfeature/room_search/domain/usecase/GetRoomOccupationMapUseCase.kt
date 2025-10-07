@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.search.subfeature.room_search.domain.usecase

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.now
import plus.vplan.app.utils.overlaps
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetRoomOccupationMapUseCase(
    private val roomRepository: RoomRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val weekRepository: WeekRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profile: Profile, date: LocalDate): Flow<List<OccupancyMapRecord>> = channelFlow {
        val schoolId = Uuid.parseHex(profile.getSchool().first().entityId)
        weekRepository.getBySchool(schoolId).map { weeks ->
            weeks.firstOrNull { LocalDate.now() in it.start..it.end }
        }.collectLatest { currentWeek ->
            combine(
                substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId, date),
                timetableRepository.getTimetableForSchool(schoolId).map { it.filter { it.dayOfWeek == date.dayOfWeek && (it.weekType == null || it.weekType == currentWeek?.weekType) } },
                weekRepository.getBySchool(schoolId),
                roomRepository.getBySchool(schoolId)
            ) { substitutionPlanLessonIds, timetableLessons, weeks, rooms ->
                val substitution = substitutionPlanLessonIds
                    .map { id -> App.substitutionPlanSource.getById(id).getFirstValueOld() }
                    .fastFilterNotNull()
                    .filter { lesson -> lesson.subject != null }

                val lessons = substitution.ifEmpty { timetableLessons }

                rooms.associateWith { room ->
                    lessons.filter { room.id in it.roomIds.orEmpty() }.map {
                        when (it) {
                            is Lesson.SubstitutionPlanLesson -> Occupancy.Lesson.fromLesson(it, date)
                            is Lesson.TimetableLesson -> Occupancy.Lesson.fromLesson(it, date)
                        }
                    }.toSet()
                }.let { map -> send(map.map { OccupancyMapRecord(it.key, it.value) }) }
            }.collect()
        }
    }
}

sealed class Occupancy(
    open val start: LocalDateTime,
    open val end: LocalDateTime
) {
    data class Lesson(val lesson: plus.vplan.app.domain.model.Lesson, val date: LocalDate, override val start: LocalDateTime, override val end: LocalDateTime) : Occupancy(start, end) {
        companion object {
            suspend fun fromLesson(lesson: plus.vplan.app.domain.model.Lesson, contextDate: LocalDate): Occupancy {
                return Lesson(lesson, contextDate, lesson.lessonTime!!.getFirstValueOld()!!.start.atDate(contextDate), lesson.lessonTime!!.getFirstValueOld()!!.end.atDate(contextDate))
            }
        }
    }
}

data class OccupancyMapRecord(
    val room: Room,
    val occupancies: Set<Occupancy>
) {
    fun isAvailableAtLessonTime(lessonTime: LessonTime): Boolean = this.occupancies.none { (lessonTime.start..lessonTime.end) overlaps (it.start.time..it.end.time) }
}