package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.IndiwareTimeTable
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.latest

private val LOGGER = Logger.withTag("UpdateTimetableUseCase")

class UpdateTimetableUseCase(
    private val indiwareRepository: IndiwareRepository,
    private val roomRepository: RoomRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val weekRepository: WeekRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val timetableRepository: TimetableRepository
) {
    suspend operator fun invoke(indiwareSchool: School.IndiwareSchool): Response.Error? {
        LOGGER.i { "Updating timetable for indiware school ${indiwareSchool.id}" }
        val rooms = roomRepository.getBySchool(indiwareSchool.id).latest()
        val teachers = teacherRepository.getBySchool(indiwareSchool.id).latest()
        val groups = groupRepository.getBySchool(indiwareSchool.id).latest()
        val weeks = weekRepository.getBySchool(indiwareSchool.id).latest()

        val weeksInPastOrCurrent = weeks
            .filter { it.end > Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .sortedBy { it.calendarWeek }

        val currentWeek = weeks.firstOrNull { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date in it.start..it.end }

        var weekIndex = weeksInPastOrCurrent.lastIndex

        var downloadedTimetable: IndiwareTimeTable? = null

        do {
            val timetable = indiwareRepository.getTimetable(
                sp24Id = indiwareSchool.sp24Id,
                username = indiwareSchool.username,
                password = indiwareSchool.password,
                week = weeksInPastOrCurrent[weekIndex],
                roomNames = rooms.map { it.name }
            )
            when {
                timetable is Response.Error.OnlineError.NotFound -> {
                    LOGGER.i { "Timetable not found for indiware school ${indiwareSchool.id} and week ${weeksInPastOrCurrent[weekIndex].calendarWeek} (retrying $weekIndex times)" }
                    weekIndex -= 1
                }
                timetable !is Response.Success && timetable is Response.Error -> return timetable
                timetable is Response.Success -> {
                    downloadedTimetable = timetable.data
                    break
                }
            }
        } while (weekIndex >= 0)

        timetableRepository.insertNewTimetable(
            schoolId = indiwareSchool.id,
            lessons = downloadedTimetable?.classes.orEmpty().flatMap { clazz ->
                val group = groups.firstOrNull { it.name == clazz.name } ?: return@flatMap emptyList()
                val lessonTimes = lessonTimeRepository.getByGroup(group.id).latest()
                clazz.lessons.map { lesson ->
                    Lesson.TimetableLesson(
                        dayOfWeek = lesson.dayOfWeek,
                        week = currentWeek ?: weeks.first(),
                        weekType = lesson.weekType,
                        subject = lesson.subject,
                        rooms = lesson.room.mapNotNull { roomName -> rooms.firstOrNull { it.name == roomName } },
                        teachers = lesson.teacher.mapNotNull { teacherName -> teachers.firstOrNull { it.name == teacherName } },
                        lessonTime = lessonTimes.first { it.lessonNumber == lesson.lessonNumber },
                        groups = listOf(group)
                    )
                }
            }
        )

        LOGGER.i { "Timetable updated for indiware school ${indiwareSchool.id}" }

        return null
    }
}