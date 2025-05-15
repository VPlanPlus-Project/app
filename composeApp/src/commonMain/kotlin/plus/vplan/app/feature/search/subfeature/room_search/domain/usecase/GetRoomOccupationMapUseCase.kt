@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.search.subfeature.room_search.domain.usecase

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.overlaps
import kotlin.uuid.ExperimentalUuidApi

class GetRoomOccupationMapUseCase(
    private val roomRepository: RoomRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val weekRepository: WeekRepository,
    private val keyValueRepository: KeyValueRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profile: Profile, date: LocalDate): Flow<List<OccupancyMapRecord>> = channelFlow {
        val schoolId = profile.getSchool().first().entityId.toInt()
        combine(
            substitutionPlanRepository.getSubstitutionPlanBySchool(schoolId, date),
            keyValueRepository.get(Keys.timetableVersion(schoolId)),
            weekRepository.getBySchool(schoolId),
            roomRepository.getBySchool(schoolId)
        ) { substitutionPlanLessonIds, timetableVersionFlow, weeks, rooms ->
            val currentWeek = weeks.firstOrNull { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date in it.start..it.end }
            val timetableVersionString = "${schoolId}_${timetableVersionFlow?.toIntOrNull() ?: -1}"

            val substitution = substitutionPlanLessonIds
                .map { id -> App.substitutionPlanSource.getById(id).getFirstValue() }
                .fastFilterNotNull()
                .filter { lesson -> lesson.subject != null }

            val lessons = substitution.ifEmpty { timetableRepository.getTimetableForSchool(schoolId, timetableVersionString).filter { it.dayOfWeek == date.dayOfWeek && (it.weekType == null || it.weekType == currentWeek?.weekType) } }

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

sealed class Occupancy(
    open val start: LocalDateTime,
    open val end: LocalDateTime
) {
    data class Lesson(val lesson: plus.vplan.app.domain.model.Lesson, val date: LocalDate, override val start: LocalDateTime, override val end: LocalDateTime) : Occupancy(start, end) {
        companion object {
            suspend fun fromLesson(lesson: plus.vplan.app.domain.model.Lesson, contextDate: LocalDate): Occupancy {
                return Lesson(lesson, contextDate, lesson.lessonTime.getFirstValue()!!.start.atDate(contextDate), lesson.lessonTime.getFirstValue()!!.end.atDate(contextDate))
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