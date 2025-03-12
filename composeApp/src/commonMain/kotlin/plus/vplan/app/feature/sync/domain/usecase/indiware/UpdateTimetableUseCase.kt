package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
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
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.utils.latest

private val LOGGER = Logger.withTag("UpdateTimetableUseCase")

class UpdateTimetableUseCase(
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
    private val indiwareRepository: IndiwareRepository,
    private val roomRepository: RoomRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val weekRepository: WeekRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val timetableRepository: TimetableRepository,
    private val profileRepository: ProfileRepository
) {

    /**
     * @param forceUpdate: Whether the app should replace its data store regardless of the hash difference
     */
    suspend operator fun invoke(indiwareSchool: School.IndiwareSchool, forceUpdate: Boolean): Response.Error? {
        LOGGER.i { "Updating timetable for indiware school ${indiwareSchool.id}" }
        val rooms = roomRepository.getBySchool(indiwareSchool.id).latest()
        val teachers = teacherRepository.getBySchool(indiwareSchool.id).latest()
        val groups = groupRepository.getBySchool(indiwareSchool.id).latest()
        val weeks = weekRepository.getBySchool(indiwareSchool.id).latest()

        val weeksInPastOrCurrent = weeks
            .filter { it.start < Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .sortedBy { it.weekIndex }

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
                    LOGGER.i { "Timetable not found for indiware school ${indiwareSchool.id} and week CW${weeksInPastOrCurrent[weekIndex].calendarWeek} (${weeksInPastOrCurrent[weekIndex].weekIndex} week of school year) (retrying $weekIndex times)" }
                    weekIndex -= 1
                }
                timetable !is Response.Success && timetable is Response.Error -> return timetable
                timetable is Response.Success -> {
                    LOGGER.i { "Timetable found for indiware school ${indiwareSchool.id} in week CW${weeksInPastOrCurrent[weekIndex].calendarWeek} (${weeksInPastOrCurrent[weekIndex].weekIndex} week of school year)" }
                    if (!timetable.data.hasChangedToPrevious) {
                        Logger.i { "No changes in timetable" + (if (!forceUpdate) ", aborting" else " but update was forced") }
                        if (!forceUpdate) return null
                    }
                    downloadedTimetable = timetable.data
                    break
                }
            }
        } while (weekIndex >= 0)

        val lessons = downloadedTimetable?.classes.orEmpty().flatMap { clazz ->
            val group = groups.firstOrNull { it.name == clazz.name } ?: return@flatMap emptyList()
            val lessonTimes = lessonTimeRepository.getByGroup(group.id).latest()
            clazz.lessons.map { lesson ->
                Lesson.TimetableLesson(
                    version = "",
                    dayOfWeek = lesson.dayOfWeek,
                    week = (currentWeek ?: weeks.first()).id,
                    weekType = lesson.weekType,
                    subject = lesson.subject,
                    rooms = lesson.room.mapNotNull { roomName -> rooms.firstOrNull { it.name == roomName } }.map { it.id },
                    teachers = lesson.teacher.mapNotNull { teacherName -> teachers.firstOrNull { it.name == teacherName } }.map { it.id },
                    lessonTime = lessonTimes.first { it.lessonNumber == lesson.lessonNumber }.id,
                    groups = listOf(group.id)
                )
            }
        }

        timetableRepository.insertNewTimetable(
            schoolId = indiwareSchool.id,
            lessons = lessons
        )

        LOGGER.i { "Timetable updated for indiware school ${indiwareSchool.id}, building caches" }
        profileRepository.getAll().first().forEach { profile ->
            updateProfileLessonIndexUseCase(profile)
        }

        return null
    }
}