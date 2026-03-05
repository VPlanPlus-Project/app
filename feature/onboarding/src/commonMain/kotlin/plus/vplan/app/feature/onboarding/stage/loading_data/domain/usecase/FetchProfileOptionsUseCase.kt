package plus.vplan.app.feature.onboarding.stage.loading_data.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.data.room.RoomRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Holiday
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateLessonTimesUseCase
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateSubjectInstanceUseCase
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateWeeksUseCase
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.lib.sp24.source.Authentication
import kotlin.time.Clock
import kotlin.uuid.Uuid

class FetchProfileOptionsUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val schoolRepository: SchoolRepository,
    private val holidayRepository: HolidayRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
) {
    suspend operator fun invoke(
        sp24SchoolId: Int,
        username: String,
        password: String,
    ): List<OnboardingProfile> {
        val client = stundenplan24Repository.getSp24Client(
            Authentication(sp24SchoolId.toString(), username, password),
            withCache = true
        )

        val schoolName = (client.getSchoolName() as? plus.vplan.lib.sp24.source.Response.Success)

        val sp24Alias = Alias(
            provider = AliasProvider.Sp24,
            value = sp24SchoolId.toString(),
            version = 1
        )

        val school = run {
            val school = School.AppSchool(
                id = Uuid.random(),
                name = schoolName?.data ?: "Unbekannte Schule",
                aliases = setOf(sp24Alias),
                cachedAt = Clock.System.now(),
                sp24Id = sp24SchoolId.toString(),
                username = username,
                password = password,
                daysPerWeek = 5,
                credentialsValid = true
            )
            schoolRepository.save(school)
        }

        val holidays = (client.holiday.getHolidays() as? plus.vplan.lib.sp24.source.Response.Success)?.data
        holidayRepository.save(holidays.orEmpty().map { Holiday(it, school.id) })

        val classes = (client.getAllClassesIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
        val groups = classes.orEmpty().associateWith { group ->
            Group.buildSp24Alias(school.sp24Id.toInt(), group.name)
        }.map { (group, alias) ->
            val group = Group(
                id = Uuid.random(),
                school = school,
                name = group.name,
                cachedAt = Clock.System.now(),
                aliases = setOf(alias)
            )
            groupRepository.save(group)
        }

        // Teachers
        val teachers = (client.getAllTeachersIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            .orEmpty().associateWith { teacher ->
                Alias(
                    provider = AliasProvider.Sp24,
                    value = "${school.sp24Id}/${teacher.name}",
                    version = 1
                )
            }.map { (teacher, aliases) ->
                teacherRepository.save(Teacher(
                    id = Uuid.random(),
                    school = school,
                    name = teacher.name,
                    cachedAt = Clock.System.now(),
                    aliases = setOf(aliases)
                ))
            }

        val rooms = (client.getAllRoomsIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            .orEmpty().associateWith { room ->
                Alias(
                    provider = AliasProvider.Sp24,
                    value = "${school.sp24Id}/${room.name}",
                    version = 1
                )
            }.forEach { (room, alias) ->
                roomRepository.save(
                    Room(
                        id = Uuid.random(),
                        school = school,
                        name = room.name,
                        cachedAt = Clock.System.now(),
                        aliases = setOf(alias)
                    )
                )
            }

        updateWeeksUseCase(school, client)
        updateLessonTimesUseCase(school, client)
        updateSubjectInstanceUseCase(school, client)

        return groups.map { group ->
            val subjectInstances = subjectInstanceRepository.getByGroup(group).first()

            Logger.d { "${group.name}: ${subjectInstances.joinToString { "${it.subject} ${it.course}" }}" }

            OnboardingProfile.StudentProfile(
                name = group.name,
                alias = group.aliases.first { it.provider == AliasProvider.Sp24 },
                subjectInstances = subjectInstances
            )
        } + teachers.map { teacher ->
            OnboardingProfile.TeacherProfile(
                name = teacher.name,
                alias = teacher.aliases.first { it.provider == AliasProvider.Sp24 }
            )
        }
    }
}