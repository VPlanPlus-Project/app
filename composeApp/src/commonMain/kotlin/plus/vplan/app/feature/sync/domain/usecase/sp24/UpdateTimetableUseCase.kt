package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.captureError
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.model.Timetable
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.takeContinuousBy
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import kotlin.uuid.Uuid
import plus.vplan.lib.sp24.source.Response as Sp24Response

private const val TAG = "UpdateTimetableUseCase"
private val LOGGER = Logger.withTag(TAG)

class UpdateTimetableUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val roomRepository: RoomRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val weekRepository: WeekRepository,
    private val timetableRepository: TimetableRepository,
    private val profileRepository: ProfileRepository
) {

    /**
     * Should be called after [UpdateWeeksUseCase]
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

        val weeks = getWeekStates(sp24School)
            .let { weeks ->
                if (forceUpdate) weeks
                else weeks
                    .filter { weekState -> weekState.relativeTime == WeekState.RelativeTime.CurrentOrFuture || (weekState.relativeTime == WeekState.RelativeTime.Past && weekState.hasData == Stundenplan24Repository.HasData.Unknown) }
                    .groupBy { it.hasData }
                    .let {
                        val unknown = it[Stundenplan24Repository.HasData.Unknown].orEmpty()

                        val other = (it[Stundenplan24Repository.HasData.Yes].orEmpty() + it[Stundenplan24Repository.HasData.No].orEmpty())
                            .sortedBy { week -> week.weekEntity.weekIndex }
                            .take(3)

                        (unknown + other)
                            .sortedBy { week -> week.weekEntity.weekIndex }
                    }
            }

        weeks.forEach forEachWeek@{ week ->
            if ((week.relativeTime == WeekState.RelativeTime.Past && week.hasData != Stundenplan24Repository.HasData.Unknown) && !forceUpdate) return@forEachWeek
            if (week.hasData != Stundenplan24Repository.HasData.Yes || forceUpdate) {
                LOGGER.d {
                    buildString {
                        append("Downloading timetable for week ${week.weekEntity.weekIndex}, starting at ${week.weekEntity.start}")
                        if (forceUpdate) append(" (FORCE UPDATE)")
                    }
                }
                val timetableResponse = sp24Client.timetable.getTimetable(week.weekEntity.weekIndex)
                val timetableMetadata = timetableRepository.getTimetableData(sp24School.id, week.weekEntity.id).first()
                    ?: Timetable(
                        id = Uuid.random(),
                        schoolId = sp24School.id,
                        weekId = week.weekEntity.id,
                        dataState = Stundenplan24Repository.HasData.Unknown
                    ).also { timetableRepository.upsertTimetable(it) }

                when (timetableResponse) {
                    is Sp24Response.Error.OnlineError.NotFound -> {
                        LOGGER.i { "Timetable not found for sp24 school ${sp24School.id} and week CW${week.weekEntity.calendarWeek} (${week.weekEntity.weekIndex} week of school year)" }
                        timetableRepository.upsertTimetable(timetableMetadata.copy(dataState = Stundenplan24Repository.HasData.No))
                        return@forEachWeek
                    }
                    is Sp24Response.Success -> {
                        LOGGER.i { "Timetable found for indiware school ${sp24School.id} in week CW${week.weekEntity.calendarWeek} (${week.weekEntity.weekIndex} week of school year)" }

                        timetableRepository.upsertTimetable(timetableMetadata.copy(dataState = Stundenplan24Repository.HasData.Yes))

                        val downloadedTimetable = timetableResponse.data
                        val lessons = downloadedTimetable.lessons.mapNotNull { lesson ->
                            val lessonGroups = lesson.classes.mapNotNull { groupName -> groups.firstOrNull { it.name == groupName } }.map { it.id }
                            if (lessonGroups.isEmpty()) {
                                captureError(
                                    location = "UpdateTimetableUseCase",
                                    message = """
                            Skipping lesson, because it does not have any groups set.
                            School: ${sp24School.sp24Id} ${sp24School.name}
                            Week: CW${week.weekEntity.calendarWeek} (${week.weekEntity.weekIndex} week of school year)
                            Lesson: ${lesson.subject} on ${lesson.dayOfWeek}, lesson number ${lesson.lessonNumber}
                            Groups configured in app: ${groups.joinToString { it.name }}
                            Groups for lesson: ${lesson.classes.joinToString()}
                        """.trimIndent()
                                )
                                return@mapNotNull null
                            }

                            Lesson.TimetableLesson(
                                dayOfWeek = DayOfWeek(lesson.dayOfWeek.isoDayNumber),
                                weekType = lesson.weekType,
                                subject = lesson.subject,
                                roomIds = lesson.rooms.mapNotNull { roomName -> rooms.firstOrNull { it.name == roomName } }.map { it.id },
                                teacherIds = lesson.teachers.mapNotNull { teacherName -> teachers.firstOrNull { it.name == teacherName } }.map { it.id },
                                groupIds = lessonGroups,
                                timetableId = timetableMetadata.id,
                                limitedToWeekIds = lesson.limitToWeekNumber
                                    ?.mapNotNull { weeks.firstOrNull { week -> week.weekEntity.weekIndex == it } }
                                    ?.map { it.weekEntity.id }
                                    ?.toSet(),
                                lessonNumber = lesson.lessonNumber,
                                weekId = week.weekEntity.id,
                                id = Uuid.random()
                            )
                        }

                        LOGGER.d { "Found ${lessons.size} lessons to insert/update" }
                        timetableRepository.upsertLessons(
                            timetableId = timetableMetadata.id,
                            lessons = lessons,
                            profiles = profileRepository.getAll().first()
                                .filterIsInstance<Profile.StudentProfile>()
                        )
                    }

                    else -> LOGGER.e { "Invalid timetable state: $timetableResponse" }
                }
            }
        }

        return null
    }

    private suspend fun getWeekStates(school: School.AppSchool): List<WeekState> {
        val today = LocalDate.now()
        val weeks = weekRepository.getBySchool(school.id).first()
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
            .map { week ->
                WeekState(
                    weekEntity = week,
                    hasData = timetableRepository.getTimetableData(school.id, week.id).first()?.dataState ?: Stundenplan24Repository.HasData.Unknown,
                    relativeTime = if (week.start < today) WeekState.RelativeTime.Past else WeekState.RelativeTime.CurrentOrFuture
                )
            }

        return weekStates.sortedBy { it.weekEntity.weekIndex }
    }
}

private data class WeekState(
    val hasData: Stundenplan24Repository.HasData,
    val relativeTime: RelativeTime,
    val weekEntity: Week
) {
    enum class RelativeTime {
        Past, CurrentOrFuture
    }
}