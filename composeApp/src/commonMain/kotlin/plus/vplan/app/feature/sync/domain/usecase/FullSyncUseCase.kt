package plus.vplan.app.feature.sync.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.capture
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomDbDto
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.schulverwalter.SyncGradesUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.until
import plus.vplan.lib.sp24.source.Authentication
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class FullSyncUseCase(
    private val schoolRepository: SchoolRepository,
    private val dayRepository: DayRepository,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val fileRepository: FileRepository,
    private val keyValueRepository: KeyValueRepository,
    private val profileRepository: ProfileRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val indiwareRepository: IndiwareRepository,
    private val roomRepository: RoomRepository,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    private val logger = Logger.withTag("FullSync")
    private val maxCacheAge = 24.hours
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
                    logger.i { "Updating schools" }
                    try {
                        val schoolStart = Clock.System.now()
                        schoolRepository.getAllLocalIds().first()
                            .mapNotNull { App.schoolSource.getById(it).getFirstValue() }
                            .forEach { school ->
                                if (school.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                                schoolRepository.getByLocalId(school.id).first()
                            }
                        val schoolEnd = Clock.System.now()
                        logger.d { "Updating schools took ${(schoolEnd - schoolStart).inWholeMilliseconds}ms" }

                        logger.i { "Updating files" }
                        val fileStart = Clock.System.now()
                        fileRepository.getAllIds().first()
                            .filter { it > 0 }
                            .mapNotNull { App.fileSource.getById(it).getFirstValueOld() }
                            .forEach { file ->
                                if (file.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                                fileRepository.getById(file.id, true).getFirstValueOld()
                            }
                        val fileEnd = Clock.System.now()
                        logger.d { "Updating files took ${(fileEnd - fileStart).inWholeMilliseconds}ms" }

                        logger.i { "Updating homework" }
                        val homeworkStart = Clock.System.now()
                        updateHomeworkUseCase(true)
                        val homeworkEnd = Clock.System.now()
                        logger.d { "Updating homework took ${(homeworkEnd - homeworkStart).inWholeMilliseconds}ms" }

                        logger.i { "Updating assessments" }
                        val assessmentStart = Clock.System.now()
                        updateAssessmentUseCase(true)
                        val assessmentEnd = Clock.System.now()
                        logger.d { "Updating assessments took ${(assessmentEnd - assessmentStart).inWholeMilliseconds}ms" }

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
                        .forEach { school ->
                            val client = indiwareRepository.getSp24Client(
                                Authentication(school.sp24Id, school.username, school.password),
                                withCache = true
                            )
                            logger.i { "Checking indiware credentials for ${school.id} (${school.name})" }
                            when (checkSp24CredentialsUseCase(school.sp24Id.toInt(), school.username, school.password)) {
                                SchoolSettingsCredentialsState.Error -> return@forEach
                                SchoolSettingsCredentialsState.Invalid -> {
                                    logger.w { "Indiware access for school ${school.id} (${school.name}) expired, sending notification" }
                                    schoolRepository.setSp24CredentialValidity(school.id, false)
                                    platformNotificationRepository.sendNotification(
                                        title = "Schulzugangsdaten abgelaufen",
                                        message = "Die Schulzugangsdaten für ${school.name} sind abgelaufen. Tippe, um sie zu aktualisieren.",
                                        category = school.name,
                                        isLarge = false,
                                        onClickData = Json.encodeToString(
                                            StartTaskJson(
                                                type = "navigate_to",
                                                value = Json.encodeToString(
                                                    StartTaskJson.StartTaskNavigateTo(
                                                        screen = "settings/school",
                                                        value = Json.encodeToString(
                                                            StartTaskJson.StartTaskNavigateTo.SchoolSettings(
                                                                openIndiwareSettingsSchoolId = school.id
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        ).also { Logger.d { "Task: $it" } }
                                    )
                                    return@forEach
                                }
                                else -> Unit
                            }

                            logger.i { "Updating rooms" }
                            val rooms = (client.getAllRoomsIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
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

                            updateTimetableUseCase(school, forceUpdate = false)
                            updateSubstitutionPlanUseCase(sp24School = school, dates = listOf(today, nextDay), allowNotification = true)
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