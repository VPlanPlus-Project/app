package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.captureError
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.takeContinuousBy
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.extension.Timetable
import kotlin.time.Duration.Companion.days

private const val TAG = "UpdateTimetableUseCase"
private val LOGGER = Logger.withTag(TAG)

class UpdateTimetableUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
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
        sp24School: School.AppSchool,
        client: Stundenplan24Client? = null,
        forceUpdate: Boolean
    ): Response.Error? {
        val sp24Client = client ?: stundenplan24Repository.getSp24Client(
            authentication = Authentication(sp24School.sp24Id, sp24School.username, sp24School.password),
            withCache = true
        )
        LOGGER.i { "Updating timetable for indiware school ${sp24School.id}" }
        val rooms = roomRepository.getBySchool(sp24School.id).first()
        val teachers = teacherRepository.getBySchool(sp24School.id).first()
        val groups = groupRepository.getBySchool(sp24School.id).first()
        val weeks = weekRepository.getBySchool(sp24School.id).first().sortedBy { it.start }
            .let { weeks ->
                val today = LocalDate.now()

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
            val today = LocalDate.now()
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

            LOGGER.w { "No current week found, using first week of school year CW${weeks.firstOrNull()?.calendarWeek} (${weeks.firstOrNull()?.weekIndex} week of school year)" }
            return@run null
        }

        var downloadedTimetable: Timetable? = null
        while (week != null) {
            fun getNextWeekToTry(): Week? {
                return weeks.lastOrNull { it.start < week!!.start && it.weekIndex == week!!.weekIndex - 1 }
            }

            val isWeekInPast = (LocalDate.now() > week.end).also {
                if (it) {
                    LOGGER.d { "Week CW${week.calendarWeek} (${week.weekIndex} week of school year) is in the past" }
                } else {
                    LOGGER.d { "Week CW${week.calendarWeek} (${week.weekIndex} week of school year) is not in the past" }
                }
            }
            val hasDataForThisWeek = stundenplan24Repository.hasTimetableForWeek(sp24School.sp24Id.toInt(), week.id)

            if (isWeekInPast && hasDataForThisWeek == false) {
                LOGGER.i { "Skipping past week CW${week.calendarWeek} (${week.weekIndex} week of school year) for sp24 school ${sp24School.id} since it doesn't have data on stundenplan24.de" }
                week = getNextWeekToTry()
                continue
            }
            val timetable = sp24Client.timetable.getTimetable(week.weekIndex)
            when {
                timetable is plus.vplan.lib.sp24.source.Response.Error.OnlineError.NotFound -> {
                    LOGGER.i { "Timetable not found for sp24 school ${sp24School.id} and week CW${week.calendarWeek} (${week.weekIndex} week of school year) (retrying ${week.weekIndex} times)" }
                    stundenplan24Repository.setHasTimetableForWeek(sp24School.sp24Id.toInt(), week.id, false)
                    week = getNextWeekToTry()
                    continue
                }

                timetable !is plus.vplan.lib.sp24.source.Response.Success && timetable is Response.Error -> return timetable
                timetable is plus.vplan.lib.sp24.source.Response.Success -> {
                    stundenplan24Repository.setHasTimetableForWeek(sp24School.sp24Id.toInt(), week.id, true)
                    LOGGER.i { "Timetable found for indiware school ${sp24School.id} in week CW${week.calendarWeek} (${week.weekIndex} week of school year)" }
                    downloadedTimetable = timetable.data
                    break
                }
            }
        }

        LOGGER.d { "Preparing lessons to insert/update" }
        val lessonTimes = lessonTimeRepository.getBySchool(sp24School.id).first()
        if (week != null) {
            val lessons = downloadedTimetable?.lessons.orEmpty().mapNotNull { lesson ->
                val lessonGroups = lesson.classes.mapNotNull { groupName -> groups.firstOrNull { it.name == groupName } }.map { it.id }
                if (lessonGroups.isEmpty()) {
                    captureError(
                        location = "UpdateTimetableUseCase",
                        message = """
                            Skipping lesson, because it does not have any groups set.
                            School: ${sp24School.sp24Id} ${sp24School.name}
                            Week: CW${week.calendarWeek} (${week.weekIndex} week of school year)
                            Lesson: ${lesson.subject} on ${lesson.dayOfWeek}, lesson number ${lesson.lessonNumber}
                            Groups configured in app: ${groups.joinToString { it.name }}
                            Groups for lesson: ${lesson.classes.joinToString()}
                        """.trimIndent()
                    )
                    return@mapNotNull null
                }
                Lesson.TimetableLesson(
                    dayOfWeek = DayOfWeek(lesson.dayOfWeek.isoDayNumber),
                    week = week.id,
                    weekType = lesson.weekType,
                    subject = lesson.subject,
                    rooms = lesson.rooms.mapNotNull { roomName -> rooms.firstOrNull { it.name == roomName } }.map { it.id },
                    teachers = lesson.teachers.mapNotNull { teacherName -> teachers.firstOrNull { it.name == teacherName } }.map { it.id },
                    groups = lessonGroups,
                    lessonTime = lessonTimes.firstOrNull { it.lessonNumber == lesson.lessonNumber && it.group in lessonGroups }?.id
                        ?: throw NullPointerException("No lesson time found for lesson ${lesson.lessonNumber} in groups ${lessonGroups.joinToString(", ")}")
                )
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
