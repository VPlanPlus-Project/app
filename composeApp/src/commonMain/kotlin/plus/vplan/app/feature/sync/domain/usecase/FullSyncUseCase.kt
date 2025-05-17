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
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.VppIdRepository
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class FullSyncUseCase(
    private val schoolRepository: SchoolRepository,
    private val dayRepository: DayRepository,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val fileRepository: FileRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val courseRepository: CourseRepository,
    private val vppIdRepository: VppIdRepository,
    private val profileRepository: ProfileRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val indiwareRepository: IndiwareRepository,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    private val logger = Logger.withTag("FullSync")
    private val maxCacheAge = 24.hours
    private var isRunning = false

    operator fun invoke(cause: FullSyncCause): Job {
        if (isRunning) {
            logger.i { "FullSync already running, this request will be ignored" }
            return Job()
        }
        return CoroutineScope(Dispatchers.IO).launch {
            isRunning = true
            capture("FullSync.Start", mapOf("cause" to cause.name))
            try {
                logger.i { "Performing FullSync" }

                val cloudDataUpdate = CoroutineScope(Dispatchers.IO).launch {
                    logger.i { "Updating groups" }
                    val groupStart = Clock.System.now()
                    groupRepository.getAllIds().first()
                        .mapNotNull { App.groupSource.getById(it).getFirstValue() }
                        .forEach { group ->
                            if (group.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            groupRepository.getById(group.id, true).getFirstValue()
                        }
                    val groupEnd = Clock.System.now()
                    logger.d { "Updating groups took ${(groupEnd - groupStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating rooms" }
                    val roomStart = Clock.System.now()
                    roomRepository.getAllIds().first()
                        .mapNotNull { App.roomSource.getById(it).getFirstValue() }
                        .forEach { room ->
                            if (room.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            roomRepository.getById(room.id, true).getFirstValue()
                        }
                    val roomEnd = Clock.System.now()
                    logger.d { "Updating rooms took ${(roomEnd - roomStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating teachers" }
                    val teacherStart = Clock.System.now()
                    teacherRepository.getAllIds().first()
                        .mapNotNull { App.teacherSource.getById(it).getFirstValue() }
                        .forEach { teacher ->
                            if (teacher.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            teacherRepository.getById(teacher.id, true).getFirstValue()
                        }
                    val teacherEnd = Clock.System.now()
                    logger.d { "Updating teachers took ${(teacherEnd - teacherStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating schools" }
                    val schoolStart = Clock.System.now()
                    schoolRepository.getAllIds().first()
                        .mapNotNull { App.schoolSource.getById(it).getFirstValue() }
                        .forEach { school ->
                            if (school.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            schoolRepository.getById(school.id, true).getFirstValue()
                        }
                    val schoolEnd = Clock.System.now()
                    logger.d { "Updating schools took ${(schoolEnd - schoolStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating files" }
                    val fileStart = Clock.System.now()
                    fileRepository.getAllIds().first()
                        .filter { it > 0 }
                        .mapNotNull { App.fileSource.getById(it).getFirstValue() }
                        .forEach { file ->
                            if (file.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            fileRepository.getById(file.id, true).getFirstValue()
                        }
                    val fileEnd = Clock.System.now()
                    logger.d { "Updating files took ${(fileEnd - fileStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating vppIds" }
                    val vppIdStart = Clock.System.now()
                    vppIdRepository.getAllIds().first()
                        .mapNotNull { App.vppIdSource.getById(it).getFirstValue() }
                        .forEach { vppId ->
                            if (vppId.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            vppIdRepository.getById(vppId.id, true).getFirstValue()
                        }
                    val vppIdEnd = Clock.System.now()
                    logger.d { "Updating vppIds took ${(vppIdEnd - vppIdStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating courses" }
                    val courseStart = Clock.System.now()
                    courseRepository.getAll().first()
                        .forEach { course ->
                            if (course.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            courseRepository.getById(course.id, true).getFirstValue()
                        }
                    val courseEnd = Clock.System.now()
                    logger.d { "Updating courses took ${(courseEnd - courseStart).inWholeMilliseconds}ms" }

                    logger.i { "Updating subject instances" }
                    val subjectInstanceStart = Clock.System.now()
                    subjectInstanceRepository.getAll().first()
                        .forEach { subjectInstance ->
                            if (subjectInstance.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                            subjectInstanceRepository.getById(subjectInstance.id, true).getFirstValue()
                        }
                    val subjectInstanceEnd = Clock.System.now()
                    logger.d { "Updating subject instances took ${(subjectInstanceEnd - subjectInstanceStart).inWholeMilliseconds}ms" }

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
                }

                val schoolDataUpdate = CoroutineScope(Dispatchers.IO).launch {
                    profileRepository.getAll().first()
                        .mapNotNull { it.getSchool().getFirstValue() }
                        .distinctBy { it.id }
                        .filterIsInstance<School.IndiwareSchool>()
                        .filter { it.credentialsValid }
                        .forEach { school ->
                            logger.i { "Checking indiware credentials for ${school.id} (${school.name})" }
                            when (checkSp24CredentialsUseCase(school.sp24Id.toInt(), school.username, school.password)) {
                                SchoolSettingsCredentialsState.Error -> return@forEach
                                SchoolSettingsCredentialsState.Invalid -> {
                                    logger.w { "Indiware access for school ${school.id} (${school.name}) expired, sending notification" }
                                    schoolRepository.setIndiwareAccessValidState(school, false)
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

                            logger.i { "BaseData update" }
                            val baseData = run downloadBaseData@{
                                val baseData = indiwareRepository.getBaseData(school.sp24Id, school.username, school.password)
                                if (baseData is Response.Error) {
                                    logger.w { "Failed to download base data for school ${school.id} (${school.name}): $baseData" }
                                    return@downloadBaseData null
                                }
                                if (baseData !is Response.Success) throw IllegalStateException("baseData is not successful: $baseData")
                                baseData.data
                            }
                            if (baseData != null) {
                                updateSubjectInstanceUseCase(school, baseData)
                                updateHolidaysUseCase(school, baseData)
                                updateWeeksUseCase(school, baseData)
                            }
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
                            updateSubstitutionPlanUseCase(school, listOf(today, nextDay), true)
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