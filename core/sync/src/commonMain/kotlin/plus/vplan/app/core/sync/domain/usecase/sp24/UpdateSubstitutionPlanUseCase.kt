package plus.vplan.app.core.sync.domain.usecase.sp24

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.day.DayRepository
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.lesson_times.LessonTimeRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.room.RoomRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.substitution_plan.SubstitutionPlanRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.data.week.WeekRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client
import plus.vplan.lib.sp24.source.isSuccess
import kotlin.uuid.Uuid

class UpdateSubstitutionPlanUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val dayRepository: DayRepository,
    private val weekRepository: WeekRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val lessonTimeRepository: LessonTimeRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
    private val profileRepository: ProfileRepository,
) {
    private val logger = Logger.withTag("UpdateSubstitutionPlanUseCase")

    sealed class Result {
        data class Success(
            val day: Day,
            val insertedLessons: List<Lesson.SubstitutionPlanLesson>,
            val profileResult: Map<Profile, ProfileResult>
        ): Result() {
            data class ProfileResult(
                val previousPlanType: PreviousPlanType,
                val previousLessons: List<Lesson>,
                val newLessons: List<Lesson>,
                val changedLessons: List<Lesson>,
            ) {
                enum class PreviousPlanType {
                    Timetable, SubstitutionPlan
                }
            }
        }

        data class Error(
            val message: String
        ): Result()
    }

    suspend operator fun invoke(
        sp24School: School.AppSchool,
        date: LocalDate,
        providedClient: Stundenplan24Client? = null,
    ): Result = withContext(Dispatchers.Default) {
        val client = providedClient ?: stundenplan24Repository.getSp24Client(
            Authentication(
                sp24School.sp24Id,
                sp24School.username,
                sp24School.password,
            ),
            withCache = true
        )

        val substitutionPlanData = client.substitutionPlan.getSubstitutionPlan(date)
        if (!substitutionPlanData.isSuccess()) {
            return@withContext Result.Error(
                substitutionPlanData.throwable?.stackTraceToString()
                    ?: substitutionPlanData.toString()
            )
        }

        val week = weekRepository.getBySchool(sp24School)
            .first()
            .find { week -> date in week.start..week.end }

        dayRepository.save(Day(
            id = Day.buildId(sp24School.id, date),
            date = date,
            school = sp24School,
            week = week,
            info = substitutionPlanData.data.info.joinToString("\n").ifBlank { null },
            dayType = Day.DayType.REGULAR,
            nextSchoolDay = null,
        ))

        val day = dayRepository.getById(Day.buildId(sp24School.id, date)).first()!!

        val teachers = teacherRepository.getBySchool(sp24School).first()
        val rooms = roomRepository.getBySchool(sp24School).first()
        val groups = groupRepository.getBySchool(sp24School).first()
        val subjectInstances = subjectInstanceRepository.getBySchool(sp24School).first()

        val lessons = substitutionPlanData.data.lessons.mapNotNull { lesson ->
            val lessonTimes = lessonTimeRepository.getByGroup(groups.firstOrNull { it.name == lesson.classes.first() } ?: run {
                logger.w { "Group ${lesson.classes.joinToString()} (specific: ${lesson.classes.first()}) not found" }
                return@mapNotNull null
            }).first()
            if (lessonTimes.isEmpty()) {
                logger.e { "No lesson times found for groups ${lesson.classes.joinToString()}" }
                return@mapNotNull null
            }

            val groupsForLesson = groups.filter { it.name in lesson.classes }

            Lesson.SubstitutionPlanLesson(
                id = Uuid.random(),
                date = date,
                weekId = week?.id,
                subject = lesson.subject,
                isSubjectChanged = lesson.subjectChanged,
                teachers = teachers.filter { it.name in lesson.teachers },
                isTeacherChanged = lesson.teachersChanged,
                rooms = rooms.filter { it.name in lesson.rooms },
                isRoomChanged = lesson.roomsChanged,
                groups = groupsForLesson,
                subjectInstance = lesson.subjectInstanceId?.let { subjectInstances.firstOrNull { it.aliases.any { alias -> alias.provider == AliasProvider.Sp24 && alias.version == 1 && alias.value.split("/").last() == lesson.subjectInstanceId.toString() } } },
                lessonNumber = lesson.lessonNumber,
                lessonTime = lessonTimes.firstOrNull { lessonTime -> lessonTime.lessonNumber == lesson.lessonNumber && lessonTime.group in groupsForLesson.map { it.id } },
                info = lesson.info,
            )
        }

        val profiles = profileRepository.getAll().first()

        val profileMappings = profiles
            .filter { it.school.id == sp24School.id }
            .associateWith { profile ->
                lessons.filter { lesson ->
                    if (profile is Profile.StudentProfile) {
                        if (profile.group.id !in lesson.groups.map { it.id }) return@filter false
                        if (lesson.subjectInstance != null && profile.subjectInstanceConfiguration.toList().firstOrNull { it.first.id == lesson.subjectInstance!!.id }?.second == false) return@filter false
                    } else if (profile is Profile.TeacherProfile) {
                        if (profile.teacher.id !in lesson.teachers.map { it.id }) return@filter false
                    }

                    return@filter true
                }
            }

        // Prepare old lessons so that we can compare them later
        val profilePreviousLessons = mutableMapOf<Profile, List<Lesson>>()
        val profilePreviousPlanType = mutableMapOf<Profile, Result.Success.ProfileResult.PreviousPlanType>()

        profiles
            .forEach { profile ->
                val substitutionLessons = substitutionPlanRepository.getForProfile(profile, date).first()
                if (substitutionLessons.isEmpty()) {
                    profilePreviousPlanType[profile] = Result.Success.ProfileResult.PreviousPlanType.Timetable
                    profilePreviousLessons[profile] = timetableRepository.getForProfile(profile).first().toList()
                } else {
                    profilePreviousPlanType[profile] = Result.Success.ProfileResult.PreviousPlanType.SubstitutionPlan
                    profilePreviousLessons[profile] = substitutionLessons
                }
            }

        // Insert new lessons
        substitutionPlanRepository
            .replaceLessons(
                date = date,
                schoolId = sp24School.id,
                lessons = lessons,
                profileMappings = profileMappings
            )

        // Compute new lessons
        val profileResult = profiles.associateWith { profile ->
            val previousLessons = profilePreviousLessons[profile]!!
            val previousLessonSignatures = previousLessons.map { it.getLessonSignature() }
            val newLessons = profileMappings[profile]!!

            val changedLessons = newLessons
                .filter { newLesson -> newLesson.getLessonSignature() !in previousLessonSignatures }

            Result.Success.ProfileResult(
                previousPlanType = profilePreviousPlanType[profile]!!,
                previousLessons = previousLessons,
                newLessons = newLessons,
                changedLessons = changedLessons
            )
        }

        return@withContext Result.Success(
            day = day,
            insertedLessons = lessons,
            profileResult = profileResult
        )
    }
}