@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.sync.domain.usecase.fullsync

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import plus.vplan.app.capture
import plus.vplan.app.captureError
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomDbDto
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolDbDto
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.TeacherDbDto
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentsUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
import plus.vplan.app.feature.system.usecase.sp24.check_sp24_credentials_validity.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.system.usecase.sp24.check_sp24_credentials_validity.SendInvalidSp24CredentialsNotification
import plus.vplan.app.feature.system.usecase.sp24.check_sp24_credentials_validity.Sp24CredentialsValidity
import plus.vplan.app.isFeatureEnabled
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Response
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class FullSyncUseCase(
    private val schoolRepository: SchoolRepository,
    private val dayRepository: DayRepository,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentsUseCase: UpdateAssessmentsUseCase,
    private val stundenplan24Repository: Stundenplan24Repository,
    private val groupRepository: GroupRepository,
    private val roomRepository: RoomRepository,
    private val teacherRepository: TeacherRepository,
    private val sendInvalidSp24CredentialsNotification: SendInvalidSp24CredentialsNotification,
) {
    private val logger = Logger.withTag("FullSync")
    private var isRunning = false

    companion object {
        var isOnboardingRunning = false
    }

    operator fun invoke(cause: FullSyncCause): Job {
        if (isOnboardingRunning) {
            logger.i { "FullSync requested while onboarding is running, this request will be ignored" }
            return Job()
        }
        if (isRunning) {
            logger.i { "FullSync already running, this request will be ignored" }
            return Job()
        }
        return CoroutineScope(Dispatchers.IO).launch {
            if (cause == FullSyncCause.Job && keyValueRepository.get(Keys.DEVELOPER_SETTINGS_ACTIVE).first().toBoolean() && keyValueRepository.get(Keys.DEVELOPER_SETTINGS_DISABLE_AUTO_SYNC).first().toBoolean()) {
                logger.i { "AutoSync was triggered but is disabled in developer settings, aborting" }
                return@launch
            }

            isRunning = true
            capture("FullSync.Start", mapOf("cause" to cause.name))
            try {
                logger.i { "Performing FullSync" }

                val cloudDataUpdate = CoroutineScope(Dispatchers.IO).launch {
                    if (isFeatureEnabled("fullsync_update-homework", true)) try {
                        updateHomeworkUseCase(true)
                    } catch (e: Exception) {
                        logger.e(e) { "Error during homework update" }
                        captureError("FullSync.HomeworkUpdate", "Error during homework update: ${e.stackTraceToString()}")
                    }

                    if (isFeatureEnabled("fullsync_update-assessments", true)) try {
                        updateAssessmentsUseCase(true)
                    } catch (e: Exception) {
                        logger.e(e) { "Error during assessments update" }
                        captureError("FullSync.AssessmentsUpdate", "Error during assessments update: ${e.stackTraceToString()}")
                    }

                    try {
                        logger.i { "Updating grades" }
                        val gradeStart = Clock.System.now()
                        syncGradesUseCase(true)
                        val gradeEnd = Clock.System.now()
                        logger.d { "Updating grades took ${(gradeEnd - gradeStart).inWholeMilliseconds}ms" }
                    } catch (e: Exception) {
                        logger.e(e) { "Error during cloud data update" }
                    }
                }

                val schoolDataUpdate = CoroutineScope(Dispatchers.IO).launch {
                    profileRepository.getAll().first()
                        .mapNotNull { it.getSchool().getFirstValue() }
                        .distinctBy { it.id }
                        .filter { it.credentialsValid }
                        .forEach forEachSchool@{ school ->
                            val client = stundenplan24Repository.getSp24Client(
                                Authentication(school.sp24Id, school.username, school.password),
                                withCache = true
                            )

                            logger.i { "Checking stundenplan24.de credentials for ${school.id} (${school.name})" }
                            val result = checkSp24CredentialsUseCase(client, Authentication(school.sp24Id, school.username, school.password))
                            if (result !is plus.vplan.app.domain.data.Response.Success) {
                                if (result is plus.vplan.app.domain.data.Response.Error.OnlineError.ConnectionError) {
                                    logger.w { "No internet connection: $result, aborting" }
                                    return@forEachSchool
                                }
                                captureError("FullSync.CheckSp24Credentials", "Failed to check credentials: $result")
                                return@forEachSchool
                            }

                            if (result.data is Sp24CredentialsValidity.Invalid.InvalidFirstTime) {
                                logger.w { "stundenplan24.de-access for school ${school.id} (${school.name}) expired, sending notification" }
                                schoolRepository.setSp24CredentialValidity(school.id, false)
                                sendInvalidSp24CredentialsNotification(school.name, school.id)
                                return@forEachSchool
                            }

                            logger.i { "Update school name" }
                            run updateSchoolName@{
                                val schoolNameResponse = (client.getSchoolName() as? Response.Success)
                                val schoolName = schoolNameResponse?.data ?: return@updateSchoolName
                                schoolRepository.upsert(SchoolDbDto.fromModel(school.copy(name = schoolName), CreationReason.Persisted))
                            }

                            logger.i { "Updating groups" }
                            run updateGroups@{
                                val groups = (client.getAllClassesIntelligent() as? Response.Success)?.data
                                groups.orEmpty().associateWith { group ->
                                    Group.buildSp24Alias(school.sp24Id.toInt(), group.name)
                                }.forEach { (group, aliases) ->
                                    groupRepository.upsert(GroupDbDto(school.id, group.name, aliases = listOf(aliases), creationReason = CreationReason.Cached))
                                }
                            }

                            logger.i { "Updating teachers" }
                            run updateTeachers@{
                                val teachers = (client.getAllTeachersIntelligent() as? Response.Success)?.data
                                teachers.orEmpty().associateWith { teacher ->
                                    Alias(
                                        provider = AliasProvider.Sp24,
                                        value = "${school.sp24Id}/${teacher.name}",
                                        version = 1
                                    )
                                }.forEach { (teacher, aliases) ->
                                    teacherRepository.upsert(
                                        TeacherDbDto(
                                            schoolId = school.id,
                                            name = teacher.name,
                                            aliases = listOf(aliases)
                                        )
                                    )
                                }
                            }

                            logger.i { "Updating rooms" }
                            run updateRooms@{
                                val rooms = (client.getAllRoomsIntelligent() as? Response.Success)?.data
                                rooms.orEmpty().associateWith { room ->
                                    Alias(
                                        provider = AliasProvider.Sp24,
                                        value = "${school.sp24Id}/${room.name}",
                                        version = 1
                                    )
                                }.forEach { (room, aliases) ->
                                    roomRepository.upsert(RoomDbDto(
                                        schoolId = school.id,
                                        name = room.name,
                                        aliases = listOf(aliases)
                                    ))
                                }
                            }

                            updateLessonTimesUseCase(school, client)
                            updateSubjectInstanceUseCase(school, client)
                            updateHolidaysUseCase(school, client)
                            updateWeeksUseCase(school, client)
                            val today = LocalDate.now()
                            val nextDay = run {
                                val holidayDates = dayRepository.getHolidays(school.id).first().map { it.date }
                                var start = today + 1.days
                                while (start.dayOfWeek.isoDayNumber > 5 || start in holidayDates) {
                                    start += 1.days
                                }
                                start
                            }

                            updateTimetableUseCase(school, client, forceUpdate = false)
                            updateSubstitutionPlanUseCase(providedClient = client, sp24School = school, dates = listOf(today, nextDay), allowNotification = true)
                        }
                }

                logger.i { "Waiting for running tasks to finish" }
                cloudDataUpdate.join()
                schoolDataUpdate.join()

                logger.i { "FullSync done" }
            } finally {
                isRunning = false
            }
        }
    }
}

enum class FullSyncCause {
    Job, Manual
}