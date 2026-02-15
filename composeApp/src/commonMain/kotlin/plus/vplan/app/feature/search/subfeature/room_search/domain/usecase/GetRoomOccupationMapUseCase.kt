@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.search.subfeature.room_search.domain.usecase

import androidx.compose.ui.util.fastFilterNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atDate
import plus.vplan.app.App
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.now
import plus.vplan.app.utils.overlaps
import kotlin.uuid.ExperimentalUuidApi

class GetRoomOccupationMapUseCase(
    private val roomRepository: RoomRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val weekRepository: WeekRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(profile: Profile, date: LocalDate): Flow<List<OccupancyMapRecord>> = channelFlow {
        combine(
            weekRepository.getBySchool(profile.school.id).map { weeks ->
                weeks.firstOrNull { LocalDate.now() in it.start..it.end }
            },
            timetableRepository.getCurrentVersion()
        ) { currentWeek, timetableVersion ->
            combine(
                substitutionPlanRepository.getSubstitutionPlanBySchool(profile.school.id, date),
                timetableRepository.getTimetableForSchool(profile.school.id, timetableVersion).map { it.filter { it.dayOfWeek == date.dayOfWeek && (it.weekType == null || it.weekType == currentWeek?.weekType) } },
                weekRepository.getBySchool(profile.school.id),
                roomRepository.getBySchool(profile.school.id)
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