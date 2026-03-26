package plus.vplan.app.feature.onboarding.stage.loading_data.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.holiday.HolidayRepository
import plus.vplan.app.core.data.room.RoomRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
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
import plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase.BuildProfileOptionsFromLocalDataUseCase
import plus.vplan.lib.sp24.source.Authentication
import kotlin.time.Clock
import kotlin.uuid.Uuid

class FetchAndStoreSchoolDataUseCase(
    private val stundenplan24Repository: Stundenplan24Repository,
    private val schoolRepository: SchoolRepository,
    private val holidayRepository: HolidayRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val buildProfileOptionsFromLocalDataUseCase: BuildProfileOptionsFromLocalDataUseCase,
) {
    suspend operator fun invoke(
        sp24SchoolId: Int,
        username: String,
        password: String,
    ): List<OnboardingProfile> = withContext(Dispatchers.Default) {
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
        classes.orEmpty().associateWith { group ->
            Group.buildSp24Alias(school.sp24Id.toInt(), group.name)
        }.forEach { (group, alias) ->
            groupRepository.save(Group(
                id = Uuid.random(),
                school = school,
                name = group.name,
                cachedAt = Clock.System.now(),
                aliases = setOf(alias)
            ))
        }

        (client.getAllTeachersIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            .orEmpty().forEach { teacher ->
                teacherRepository.save(Teacher(
                    id = Uuid.random(),
                    school = school,
                    name = teacher.name,
                    cachedAt = Clock.System.now(),
                    aliases = setOf(Alias(
                        provider = AliasProvider.Sp24,
                        value = "${school.sp24Id}/${teacher.name}",
                        version = 1
                    ))
                ))
            }

        (client.getAllRoomsIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            .orEmpty().forEach { room ->
                roomRepository.save(Room(
                    id = Uuid.random(),
                    school = school,
                    name = room.name,
                    cachedAt = Clock.System.now(),
                    aliases = setOf(Alias(
                        provider = AliasProvider.Sp24,
                        value = "${school.sp24Id}/${room.name}",
                        version = 1
                    ))
                ))
            }

        updateWeeksUseCase(school, client)
        updateLessonTimesUseCase(school, client)
        updateSubjectInstanceUseCase(school, client)

        return@withContext buildProfileOptionsFromLocalDataUseCase(school)
    }
}
