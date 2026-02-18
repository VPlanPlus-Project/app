@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.search.subfeature.room_search.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atDate
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Room
import plus.vplan.app.domain.model.populated.LessonPopulator
import plus.vplan.app.domain.model.populated.PopulatedLesson
import plus.vplan.app.domain.model.populated.PopulationContext
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
    private val weekRepository: WeekRepository,
    private val lessonPopulator: LessonPopulator,
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
                substitutionPlanRepository.getSubstitutionPlanBySchool(profile.school.id, date)
                    .flatMapLatest { lessons -> lessonPopulator.populateMultiple(lessons.toList(), PopulationContext.Profile(profile)) }
                    .map { lessons -> lessons.map { it as PopulatedLesson.SubstitutionPlanLesson } },
                timetableRepository.getTimetableForSchool(profile.school.id, timetableVersion)
                    .map { it.filter { it.dayOfWeek == date.dayOfWeek && (it.weekType == null || it.weekType == currentWeek?.weekType) } }
                    .flatMapLatest { lessons -> lessonPopulator.populateMultiple(lessons, PopulationContext.Profile(profile)) }
                    .map { lessons -> lessons.map { it as PopulatedLesson.TimetableLesson } },
                weekRepository.getBySchool(profile.school.id),
                roomRepository.getBySchool(profile.school.id)
            ) { substitutionPlanLessons, timetableLessons, weeks, rooms ->
                val substitution = substitutionPlanLessons
                    .filter { lesson -> lesson.lesson.subject != null }

                val lessons = substitution.ifEmpty { timetableLessons }

                rooms.associateWith { room ->
                    lessons.filter { room.id in it.lesson.roomIds.orEmpty() }.map {
                        when (it) {
                            is PopulatedLesson.SubstitutionPlanLesson -> Occupancy.Lesson.fromLesson(it, date)
                            is PopulatedLesson.TimetableLesson -> Occupancy.Lesson.fromLesson(it, date)
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
    data class Lesson(val lesson: PopulatedLesson, val date: LocalDate, override val start: LocalDateTime, override val end: LocalDateTime) : Occupancy(start, end) {
        companion object {
            suspend fun fromLesson(lesson: PopulatedLesson, contextDate: LocalDate): Occupancy {
                return Lesson(lesson, contextDate, lesson.lessonTime!!.start.atDate(contextDate), lesson.lessonTime!!.end.atDate(contextDate))
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