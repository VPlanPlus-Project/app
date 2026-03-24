package plus.vplan.app.core.sync.domain.usecase.sp24

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.lesson_times.LessonTimeRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.room.RoomRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.data.week.WeekRepository
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Timetable
import plus.vplan.app.core.model.Week
import plus.vplan.app.core.utils.date.atStartOfWeek
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.list.takeContinuousBy
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Response
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.isSuccess
import kotlin.uuid.Uuid

class UpdateTimetableUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val timetableRepository: TimetableRepository,
    private val roomRepository: RoomRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val weekRepository: WeekRepository,
    private val profileRepository: ProfileRepository,
) {

    sealed class Result {
        data object Success: Result()
        data object NoDataForWeek: Result()
        data class Error(val message: String): Result()
    }

    suspend operator fun invoke(
        sp24School: School.AppSchool,
        week: Week,
        providedClient: Stundenplan24Client? = null
    ) = withContext(Dispatchers.Default) {
        val sp24Client = providedClient ?: stundenplan24Repository.getSp24Client(
            authentication = Authentication(sp24School.sp24Id, sp24School.username, sp24School.password),
            withCache = true
        )

        val timetableResponse = sp24Client.timetable.getTimetable(week.weekIndex)
        if (!timetableResponse.isSuccess() && timetableResponse !is Response.Error.OnlineError.NotFound) {
            return@withContext Result.Error(timetableResponse.throwable?.stackTraceToString() ?: timetableResponse.toString())
        }

        val oldTimetable = timetableRepository.getTimetableData(
            schoolId = sp24School.id,
            weekId = week.id
        ).first()

        val dataState = if (timetableResponse.isSuccess()) Timetable.HasData.Yes
        else Timetable.HasData.No

        val newTimetable = oldTimetable?.copy(
            week = week,
            dataState = dataState
        ) ?: Timetable(
            id = Uuid.random(),
            week = week,
            dataState = dataState,
            schoolId = sp24School.id
        )

        timetableRepository.upsertTimetable(newTimetable)

        if (!timetableResponse.isSuccess()) {
            require(timetableResponse is Response.Error.OnlineError.NotFound)
            return@withContext Result.NoDataForWeek
        }

        val rooms = roomRepository.getBySchool(sp24School).first()
        val teachers = teacherRepository.getBySchool(sp24School).first()
        val groups = groupRepository.getBySchool(sp24School).first()
        val weeks = getWeekStates(sp24School)

        val lessons = timetableResponse.data.lessons.mapNotNull { lesson ->
            val lessonGroups = lesson.classes.mapNotNull { groupName -> groups.firstOrNull { it.name == groupName } }
            if (lessonGroups.isEmpty()) {
                analyticsRepository.captureError(
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

            val lessonTime = if (lessonGroups.isEmpty()) null
            else lessonTimeRepository
                .getByGroup(lessonGroups.first(), lesson.lessonNumber)
                .first()

            Lesson.TimetableLesson(
                dayOfWeek = DayOfWeek(lesson.dayOfWeek.isoDayNumber),
                weekType = lesson.weekType,
                subject = lesson.subject,
                rooms = lesson.rooms.mapNotNull { roomName -> rooms.firstOrNull { it.name == roomName } },
                teachers = lesson.teachers.mapNotNull { teacherName -> teachers.firstOrNull { it.name == teacherName } },
                groups = lessonGroups,
                timetableId = newTimetable.id,
                limitedToWeeks = lesson.limitToWeekNumber
                    ?.mapNotNull { weeks.firstOrNull { week -> week.weekIndex == it } },
                lessonNumber = lesson.lessonNumber,
                weekId = week.id,
                id = Uuid.random(),
                lessonTime = lessonTime
            )
        }

        val profiles = profileRepository.getAll().first()

        val profileMappings = profiles
            .filter { it.school.id == sp24School.id }
            .associateWith { profile ->
                lessons.filter { lesson ->
                    if (profile is Profile.StudentProfile) {
                        if (profile.group.id !in lesson.groups.map { it.id }) return@filter false
                    } else if (profile is Profile.TeacherProfile) {
                        if (profile.teacher.id !in lesson.teachers.map { it.id }) return@filter false
                    }

                    return@filter true
                }
            }

        timetableRepository
            .upsertLessons(
                timetableId = newTimetable.id,
                lessons = lessons,
                profileMapping = profileMappings,
            )

        return@withContext Result.Success
    }


    /**
     * Will backtrack the week for the most recent timetable that is valid for the date.
     */
    suspend fun updateTimetableRelatedToDate(date: LocalDate, school: School.AppSchool) {
        val weeks = getWeekStates(school)
        val elapsedWeeksIncludingCurrent = weeks
            .filter { week -> week.start <= date }
            .sortedBy { it.weekIndex }
            .reversed()

        for (week in elapsedWeeksIncludingCurrent) {
            val timetable = timetableRepository.getTimetableData(schoolId = school.id, weekId = week.id).first()
            if (timetable?.dataState == Timetable.HasData.No) continue
            val result = invoke(school, week)
            if (result == Result.NoDataForWeek) continue
            if (result is Result.Success) return
        }
    }

    /**
     * Used to get weeks for the current school year as we have no way of knowing which weeks
     * belong to a school year since the concept of school years does not exist in Stundenplan24.
     */
    private suspend fun getWeekStates(school: School.AppSchool): List<Week> {
        val today = LocalDate.now()
        val weeks = weekRepository.getBySchool(school).first()
        if (weeks.isEmpty()) return emptyList()

        fun getAllWeeksContainingWeek(week: Week): List<Week>? {
            val firstWeek = weeks
                .filter { it.start < week.start }
                .filter { it.weekIndex == 1 }
                .maxByOrNull { it.start }
            if (firstWeek == null) return null
            return weeks
                .dropWhile { it != firstWeek }
                .takeContinuousBy { it.weekIndex }
        }

        val weekStates = weeks
            .sortedBy { it.start }
            .let findWeeksWithMultipleSchoolYears@{ weeks ->
                if (weeks.count { it.weekIndex == 1 } == 1) weeks
                else {
                    // There are multiple school years in the app so we need to
                    // find the most appropriate for the given date.

                    // Check if there is a week which is currently ongoing
                    weeks
                        .firstOrNull { it.start == today.atStartOfWeek() }
                        ?.let { currentWeek ->
                            getAllWeeksContainingWeek(currentWeek)?.let {
                                return@findWeeksWithMultipleSchoolYears it
                            }
                        }

                    // Find the next week starting after today
                    weeks
                        .filter { it.start > today }
                        .minByOrNull { it.start }
                        ?.let { nextWeek ->
                            getAllWeeksContainingWeek(nextWeek)?.let {
                                return@findWeeksWithMultipleSchoolYears it
                            }
                        }

                    return emptyList()
                }
            }

        return weekStates.sortedBy { it.weekIndex }
    }
}