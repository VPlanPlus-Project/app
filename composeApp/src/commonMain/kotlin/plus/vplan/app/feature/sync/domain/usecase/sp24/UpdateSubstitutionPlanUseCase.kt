@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atDate
import kotlinx.datetime.format
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.untilRelativeText
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val LOGGER = Logger.withTag("UpdateSubstitutionPlanUseCase")

class UpdateSubstitutionPlanUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val weekRepository: WeekRepository,
    private val dayRepository: DayRepository,
    private val profileRepository: ProfileRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val timetableRepository: TimetableRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase
) {
    suspend operator fun invoke(
        sp24School: School.AppSchool,
        dates: List<LocalDate>,
        providedClient: Stundenplan24Client? = null,
        allowNotification: Boolean
    ): Response.Error? {
        val client = providedClient ?: stundenplan24Repository.getSp24Client(
            Authentication(
                sp24School.sp24Id,
                sp24School.username,
                sp24School.password,
            ),
            withCache = true
        )
        val teachers = teacherRepository.getBySchool(sp24School.id).first()
        val rooms = roomRepository.getBySchool(sp24School.id).first()
        val groups = groupRepository.getBySchool(sp24School.id).first()
        val subjectInstances = subjectInstanceRepository.getBySchool(sp24School.id).first()
        var error: Response.Error? = null

        val studentProfilesForSchool = profileRepository.getAll().first()
        .filterIsInstance<Profile.StudentProfile>()
            .filter { it.school.id == sp24School.id }

        val insertVersion = substitutionPlanRepository.getCurrentVersion().first() + 1

        val profileLessons = studentProfilesForSchool
            .associateWith { profile ->
                dates.map { date ->
                    ProfileLessonChanges(
                        date = date,
                        oldLessons = substitutionPlanRepository.getForProfile(profile, date, insertVersion - 1).first()
                    )
                }
            }

        dates.forEach forEachDate@{ date ->
            val week = weekRepository.getBySchool(sp24School.id).first().firstOrNull { date in it.start..it.end } ?: run {
                val errorMessage = "Week for $date not found"
                Logger.d { errorMessage }
                error = Response.Error.Other(errorMessage)
                null
            }

            val substitutionPlanResponse = client.substitutionPlan.getSubstitutionPlan(date)
            if (substitutionPlanResponse is plus.vplan.lib.sp24.source.Response.Error.OnlineError.NotFound) return@forEachDate

            if (substitutionPlanResponse is plus.vplan.lib.sp24.source.Response.Error) run {
                error = Response.Error.fromSp24KtError(substitutionPlanResponse)
                return@forEachDate
            }
            if (substitutionPlanResponse !is plus.vplan.lib.sp24.source.Response.Success) throw IllegalStateException("substitutionPlanResponse is not successful: $substitutionPlanResponse")

            val substitutionPlan = substitutionPlanResponse.data

            val lessonsForDay = mutableListOf<Lesson.SubstitutionPlanLesson>()

            val day = Day(
                id = Day.buildId(sp24School, date),
                date = date,
                schoolId = sp24School.id,
                weekId = week?.id,
                info = substitutionPlan.info.joinToString("\n").ifBlank { null },
                dayType = Day.DayType.REGULAR,
                substitutionPlan = emptySet(),
                timetable = emptySet(),
                assessmentIds = emptySet(),
                homeworkIds = emptySet(),
                nextSchoolDayId = null,
                tags = emptySet()
            )

            dayRepository.insert(day)

            substitutionPlan.lessons.mapNotNull { lesson ->
                val lessonTimes = lessonTimeRepository.getByGroup(groups.firstOrNull { it.name == lesson.classes.first() }?.id ?: run {
                    LOGGER.w { "Group ${lesson.classes.joinToString()} (specific: ${lesson.classes.first()}) not found" }
                    return@mapNotNull null
                }).first()
                if (lessonTimes.isEmpty()) {
                    Logger.e { "No lesson times found for groups ${lesson.classes.joinToString()}" }
                    return@mapNotNull null
                }

                Lesson.SubstitutionPlanLesson(
                    id = Uuid.random(),
                    date = date,
                    weekId = week?.id,
                    subject = lesson.subject,
                    isSubjectChanged = lesson.subjectChanged,
                    teacherIds = teachers.filter { it.name in lesson.teachers }.map { it.id },
                    isTeacherChanged = lesson.teachersChanged,
                    roomIds = rooms.filter { it.name in lesson.rooms }.map { it.id },
                    isRoomChanged = lesson.roomsChanged,
                    groupIds = groups.filter { it.name in lesson.classes }.map { it.id },
                    subjectInstanceId = lesson.subjectInstanceId?.let { subjectInstances.firstOrNull { it.aliases.any { alias -> alias.provider == AliasProvider.Sp24 && alias.version == 1 && alias.value.split("/").last() == lesson.subjectInstanceId.toString() } } }?.id,
                    lessonNumber = lesson.lessonNumber,
                    lessonTimeId = lessonTimes.first().id,
                    info = lesson.info
                )
            }.let { lessonsForDay.addAll(it) }

            substitutionPlanRepository.upsertLessons(
                schoolId = sp24School.id,
                date = date,
                lessons = lessonsForDay,
                version = insertVersion,
            )
        }

        val timetableVersion = timetableRepository.getCurrentVersion().first()
        studentProfilesForSchool.forEach { profile ->
            updateProfileLessonIndexUseCase(profile, insertVersion, timetableVersion)
        }

        if (allowNotification) profileLessons.forEach { (profile, oldLessonsMaps) ->
            dates.forEach forEachDate@{ date ->
                val oldLessons = oldLessonsMaps
                    .firstOrNull { it.date == date }?.oldLessons.orEmpty()
                    .associateWith { it.getLessonSignature() }

                val newLessons = substitutionPlanRepository.getForProfile(
                    profile = profile,
                    date = date,
                    version = insertVersion
                ).first()
                    .associateWith { it.getLessonSignature() }

                // Skip if last lesson ends in past (notification is not important anymore)
                if ((oldLessons + newLessons).keys.mapNotNull { it.lessonTime?.getFirstValueOld()?.end?.atDate(date) }.maxOrNull()?.let { it < LocalDateTime.now() } == true) return@forEachDate

                val changedOrNewLessons = newLessons
                    .filter { (lesson, signature) -> signature !in oldLessons.values }
                    .keys
                if (changedOrNewLessons.isEmpty()) return@forEachDate

                Logger.d { "Sending notification for ${profile.name}" }

                val newDay = App.daySource.getById("${sp24School.id}/$date", profile).getFirstValueOld()

                val changedLessons = changedOrNewLessons
                    .associateWith { it.lessonNumber }
                    .toList()
                    .sortedBy { it.second }
                    .map { it.first }

                if (changedLessons.isNotEmpty()) {
                    Logger.d { "Sending notification for ${profile.name} with changed lessons: $changedLessons" }
                    platformNotificationRepository.sendNotification(
                        title = "Neuer Plan (${(LocalDate.now() untilRelativeText date) ?: date.format(regularDateFormat)})",
                        message = "Es gibt ${changedOrNewLessons.size} Änderungen für dich",
                        largeText = buildString {
                            changedLessons.forEachIndexed { i, lesson ->
                                if (i > 0) append("\n")
                                append(lesson.lessonNumber)
                                append(". ")
                                append(lesson.subject ?: "Entfall")
                                if (lesson.teacherIds.isNotEmpty()) {
                                    append(" mit ")
                                    append(lesson.teachers.first().filterIsInstance<AliasState.Done<Teacher>>().joinToString(", ") { it.data.name })
                                }
                                if (lesson.roomIds.orEmpty().isNotEmpty()) {
                                    append(" in ")
                                    append(lesson.rooms.first().filterIsInstance<AliasState.Done<Room>>().joinToString(", ") { it.data.name })
                                }
                            }
                            if (newDay?.info != null) append("\n\nℹ\uFE0F ${newDay.info}")
                        }.dropLastWhile { it == '\n' }.dropWhile { it == '\n' },
                        category = profile.name,
                        isLarge = true,
                        onClickData = Json.encodeToString(
                            StartTaskJson(
                                type = "navigate_to",
                                profileId = profile.id.toString(),
                                value = Json.encodeToString(
                                    StartTaskJson.StartTaskNavigateTo(
                                        screen = "calendar",
                                        value = Json.encodeToString(
                                            StartTaskJson.StartTaskNavigateTo.StartTaskCalendar(
                                                date = date.toString()
                                            )
                                        )
                                    )
                                )
                            )
                        ).also { Logger.d { "Task: $it" } }
                    )
                }
            }
        }

        return error
    }
}

private data class ProfileLessonChanges(
    val date: LocalDate,
    val oldLessons: List<Lesson>,
)