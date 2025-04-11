@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.feature.sync.domain.usecase.indiware

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Teacher
import plus.vplan.app.domain.model.findByIndiwareId
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.LessonTimeRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.WeekRepository
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.utils.latest
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val LOGGER = Logger.withTag("UpdateSubstitutionPlanUseCase")

class UpdateSubstitutionPlanUseCase(
    private val getDayUseCase: GetDayUseCase,
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
    private val indiwareRepository: IndiwareRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val weekRepository: WeekRepository,
    private val dayRepository: DayRepository,
    private val profileRepository: ProfileRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(
        indiwareSchool: School.IndiwareSchool,
        dates: List<LocalDate>,
        allowNotification: Boolean
    ): Response.Error? {
        val teachers = teacherRepository.getBySchool(indiwareSchool.id).latest()
        val rooms = roomRepository.getBySchool(indiwareSchool.id).latest()
        val groups = groupRepository.getBySchool(indiwareSchool.id).latest()
        val subjectInstances = subjectInstanceRepository.getBySchool(indiwareSchool.id, false).latest()
        val lessons = mutableListOf<Lesson.SubstitutionPlanLesson>()
        var error: Response.Error? = null
        dates.forEach forEachDate@{ date ->
            val week = weekRepository.getBySchool(indiwareSchool.id).latest().firstOrNull { date in it.start..it.end } ?: run {
                error = Response.Error.Other("Week for $date not found")
                return@forEachDate
            }

            val oldPlan = profileRepository.getAll().first()
                .filterIsInstance<Profile.StudentProfile>()
                .filter { it.getSchoolItem().id == indiwareSchool.id }
                .associateWith { getDayUseCase(it, date).first().lessons.first() }

            val substitutionPlanResponse = indiwareRepository.getSubstitutionPlan(
                sp24Id = indiwareSchool.sp24Id,
                username = indiwareSchool.username,
                password = indiwareSchool.password,
                date = date,
                teacherNames = teachers.map { it.name },
                roomNames = rooms.map { it.name }
            )
            if (substitutionPlanResponse is Response.Error.OnlineError.NotFound) return@forEachDate

            if (substitutionPlanResponse is Response.Error) run {
                error = substitutionPlanResponse
                return@forEachDate
            }
            if (substitutionPlanResponse !is Response.Success) throw IllegalStateException("substitutionPlanResponse is not successful: $substitutionPlanResponse")

            val substitutionPlan = substitutionPlanResponse.data

            val day = Day(
                id = Day.buildId(indiwareSchool, date),
                date = date,
                schoolId = indiwareSchool.id,
                weekId = week.id,
                info = substitutionPlan.info,
                dayType = Day.DayType.REGULAR,
                substitutionPlan = emptySet(),
                timetable = emptySet(),
                assessmentIds = emptySet(),
                homeworkIds = emptySet(),
                nextSchoolDayId = null
            )

            dayRepository.insert(day)

            substitutionPlan.classes.flatMap { substitutionPlanClass ->
                val group = groups.firstOrNull { it.name == substitutionPlanClass.name } ?: run {
                    LOGGER.w { "Group ${substitutionPlanClass.name} not found" }
                    return@flatMap emptyList()
                }
                var lessonTimes = lessonTimeRepository.getByGroup(group.id).latest()
                if (lessonTimes.isEmpty()) {
                    Logger.e { "No lesson times found for group ${group.name}" }
                    return@flatMap emptyList()
                }
                while (substitutionPlanClass.lessons.any { it.lessonNumber > lessonTimes.maxOf { it.lessonNumber } }) {
                    val nextLessonTime = lessonTimes.maxBy { it.lessonNumber }
                    val newLessonTime = LessonTime(
                        id = "${group.schoolId}/${group.id}/${nextLessonTime.lessonNumber + 1}",
                        start = nextLessonTime.end,
                        end = nextLessonTime.end + 45.minutes,
                        lessonNumber = nextLessonTime.lessonNumber + 1,
                        group = group.id,
                        interpolated = true
                    )
                    lessonTimeRepository.upsert(newLessonTime)
                    lessonTimes = lessonTimeRepository.getByGroup(group.id).latest()
                }
                substitutionPlanClass.lessons.mapNotNull { substitutionPlanLesson ->
                    Lesson.SubstitutionPlanLesson(
                        id = Uuid.random(),
                        date = date,
                        week = week.id,
                        subject = substitutionPlanLesson.subject,
                        isSubjectChanged = substitutionPlanLesson.subjectChanged,
                        teacherIds = teachers.filter { it.name in substitutionPlanLesson.teacher }.map { it.id },
                        isTeacherChanged = substitutionPlanLesson.teacherChanged,
                        roomIds = rooms.filter { it.name in substitutionPlanLesson.room }.map { it.id },
                        isRoomChanged = substitutionPlanLesson.roomChanged,
                        groupIds = listOf(group.id),
                        subjectInstanceId = substitutionPlanLesson.subjectInstanceNumber?.let { subjectInstances.findByIndiwareId(it.toString()) }?.id,
                        lessonTimeId = lessonTimes.firstOrNull { it.lessonNumber == substitutionPlanLesson.lessonNumber }?.id ?: run {
                            Logger.e { "No lesson time found for lesson number ${substitutionPlanLesson.lessonNumber} in group ${group.name} at $date" }
                            return@mapNotNull null
                        },
                        version = "",
                        info = substitutionPlanLesson.info
                    )
                }
            }.let { lessons.addAll(it) }

            val newVersion = keyValueRepository.get(Keys.substitutionPlanVersion(indiwareSchool.id)).first()
            val newPlan = profileRepository.getAll().first()
                .filterIsInstance<Profile.StudentProfile>()
                .filter { it.getSchoolItem().id == indiwareSchool.id }
                .associateWith {
                    substitutionPlanRepository.getSubstitutionPlanBySchool(indiwareSchool.id, date, "${indiwareSchool.id}_$newVersion")
                        .mapNotNull { App.substitutionPlanSource.getById(it).getFirstValue() }
                        .filter { lesson -> lesson.isRelevantForProfile(it) }
                }

            if (allowNotification) profileRepository.getAll().first()
                .filterIsInstance<Profile.StudentProfile>()
                .filter { it.getSchoolItem().id == indiwareSchool.id }
                .forEach forEachProfile@{ profile ->
                    val old = oldPlan[profile] ?: return@forEachProfile
                    val new = newPlan[profile] ?: return@forEachProfile

                    val oldLessons = old.map { it.getLessonSignature() }
                    val changedOrNewLessons = new.filter { it.getLessonSignature() !in oldLessons }
                    if (changedOrNewLessons.isEmpty()) return@forEachProfile

                    Logger.d { "Sending notification for ${profile.name}" }

                    val newDay = App.daySource.getById("${indiwareSchool.id}/$date", profile).getFirstValue()

                    val changedLessons = changedOrNewLessons.filter { it.isRelevantForProfile(profile) }
                    if (changedLessons.isNotEmpty()) {
                        Logger.d { "Sending notification for ${profile.name} with changed lessons: $changedLessons" }
                        platformNotificationRepository.sendNotification(
                            title = "Neuer Plan (${(LocalDate.now() untilRelativeText date) ?: date.format(regularDateFormat)})",
                            message = "Es gibt ${changedOrNewLessons.size} Änderungen für dich",
                            largeText = buildString {
                                changedLessons.forEachIndexed { i, lesson ->
                                    if (i > 0) append("\n")
                                    append(lesson.lessonTime.getFirstValue()?.lessonNumber)
                                    append(". ")
                                    append(lesson.subject ?: "Entfall")
                                    if (lesson.teacherIds.isNotEmpty()) {
                                        append(" mit ")
                                        append(lesson.teachers.first().filterIsInstance<CacheState.Done<Teacher>>().joinToString(", ") { it.data.name })
                                    }
                                    if (lesson.roomIds.isNotEmpty()) {
                                        append(" in ")
                                        append(lesson.rooms.first().filterIsInstance<CacheState.Done<Room>>().joinToString(", ") { it.data.name })
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

        substitutionPlanRepository.insertNewSubstitutionPlan(indiwareSchool.id, lessons)

        LOGGER.i { "Substitution plan updated for indiware school ${indiwareSchool.id}, building caches" }
        profileRepository.getAll().first().forEach { profile ->
            updateProfileLessonIndexUseCase(profile)
        }

        return error
    }
}