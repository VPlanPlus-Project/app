package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
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
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.takeContinuousBy
import kotlin.time.Duration.Companion.days

private val TAG = "UpdateTimetableUseCase"
private val LOGGER = Logger.withTag(TAG)

class UpdateTimetableUseCase(
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
    suspend operator fun invoke(
        sp24School: School.Sp24School,
        forceUpdate: Boolean
    ): Response.Error? {
        LOGGER.i { "Updating timetable for indiware school ${sp24School.id}" }
        val rooms = roomRepository.getBySchool(sp24School.id).first()
        val teachers = teacherRepository.getBySchool(sp24School.id).first()
        val groups = groupRepository.getBySchool(sp24School.id).first()
        val weeks = weekRepository.getBySchool(sp24School.id).first().sortedBy { it.start }
            .let { weeks ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

                val firstSchoolWeekOfCurrentYear =
                    weeks.firstOrNull { it.start.year == today.year && it.weekIndex == 1 }
                if (firstSchoolWeekOfCurrentYear != null && firstSchoolWeekOfCurrentYear.start <= today) {
                    // We are in the first half of the school year (it started in the current year)
                    return@let weeks.dropWhile { it != firstSchoolWeekOfCurrentYear }
                        .takeContinuousBy { it.weekIndex }
                }

                val firstSchoolWeekOfPreviousYear =
                    weeks.firstOrNull { it.start.year == today.year - 1 && it.weekIndex == 1 }
                if (firstSchoolWeekOfPreviousYear != null && firstSchoolWeekOfPreviousYear.start < today) {
                    // We are in the second half of the school year (it started in the previous year)
                    return@let weeks.dropWhile { it != firstSchoolWeekOfPreviousYear }
                        .takeContinuousBy { it.weekIndex }
                }

                // No weeks for the current school year found, if next school year is already available, use it
                if (firstSchoolWeekOfCurrentYear != null) {
                    LOGGER.w { "No weeks for the current school year found, using weeks from next school year" }
                    return@let weeks.dropWhile { it != firstSchoolWeekOfCurrentYear }
                        .takeContinuousBy { it.weekIndex }
                }
                return@let emptyList()
            }


        LOGGER.d { "Found ${rooms.size} rooms, ${teachers.size} teachers, ${groups.size} groups and ${weeks.size} weeks" }

        var week = run {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val currentWeek =
                weeks.firstOrNull { today in it.start..it.end.atStartOfWeek().plus(7.days) }

            if (currentWeek != null) {
                LOGGER.d { "Current week is CW${currentWeek.calendarWeek} (${currentWeek.weekIndex} week of school year)" }
                return@run currentWeek
            }

            val startOfNextSchoolYear = weeks.firstOrNull { it.start > today && it.weekIndex == 1 }
            if (startOfNextSchoolYear != null) {
                LOGGER.d { "No current week found, using first week of next school year CW${startOfNextSchoolYear.calendarWeek} (${startOfNextSchoolYear.weekIndex} week of school year)" }
                return@run startOfNextSchoolYear
            }

            LOGGER.w { "No current week found, using first week of school year CW${weeks.first().calendarWeek} (${weeks.first().weekIndex} week of school year)" }
            return@run null
        }

        var downloadedTimetable: IndiwareTimeTable? = null
        while (week != null) {
            val timetable = indiwareRepository.getTimetable(
                sp24Id = sp24School.sp24Id,
                username = sp24School.username,
                password = sp24School.password,
                week = week,
                roomNames = rooms.map { it.name }
            )
            when {
                timetable is Response.Error.OnlineError.NotFound -> {
                    LOGGER.i { "Timetable not found for indiware school ${sp24School.id} and week CW${week.calendarWeek} (${week.weekIndex} week of school year) (retrying ${week.weekIndex} times)" }
                    week = weeks.lastOrNull { it.start < week.start && it.weekIndex == week.weekIndex - 1 }
                }

                timetable !is Response.Success && timetable is Response.Error -> return timetable
                timetable is Response.Success -> {
                    LOGGER.i { "Timetable found for indiware school ${sp24School.id} in week CW${week.calendarWeek} (${week.weekIndex} week of school year)" }
                    if (!timetable.data.hasChangedToPrevious) {
                        Logger.i { "No changes in timetable" + (if (!forceUpdate) ", aborting" else " but update was forced") }
                        if (!forceUpdate) return null
                    }
                    downloadedTimetable = timetable.data
                    break
                }
            }
        }

        LOGGER.d { "Preparing lessons to insert/update" }
        if (week != null) {
            val lessons = downloadedTimetable?.classes.orEmpty().flatMap { clazz ->
                val group =
                    groups.firstOrNull { it.name == clazz.name } ?: return@flatMap emptyList()
                val lessonTimes = lessonTimeRepository.getByGroup(group.id).first()
                clazz.lessons.map { lesson ->
                    val lessonTime =
                        lessonTimes.firstOrNull { it.lessonNumber == lesson.lessonNumber } ?: run {
                            LOGGER.w { "No lesson time found for lesson number ${lesson.lessonNumber} in group ${group.name}, skipping lesson" }
                            null!!
                        }

                    Lesson.TimetableLesson(
                        dayOfWeek = lesson.dayOfWeek,
                        week = week.id,
                        weekType = lesson.weekType,
                        subject = lesson.subject,
                        rooms = lesson.room.mapNotNull { roomName -> rooms.firstOrNull { it.name == roomName } }
                            .map { it.id },
                        teachers = lesson.teacher.mapNotNull { teacherName -> teachers.firstOrNull { it.name == teacherName } }
                            .map { it.id },
                        lessonTime = lessonTime.id,
                        groups = listOf(group.id)
                    )
                }
            }

            LOGGER.d { "Found ${lessons.size} lessons to insert/update" }
            timetableRepository.upsertLessons(
                schoolId = sp24School.id,
                lessons = lessons,
                profiles = profileRepository.getAll().first()
                    .filterIsInstance<Profile.StudentProfile>()
            )
        }
        LOGGER.d { "Finished inserting lessons" }

        return null
    }
}
