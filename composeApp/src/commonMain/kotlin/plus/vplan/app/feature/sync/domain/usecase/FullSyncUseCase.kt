package plus.vplan.app.feature.sync.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.FileRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateDefaultLessonsUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
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
    private val vppIdRepository: VppIdRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateDefaultLessonsUseCase: UpdateDefaultLessonsUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase,
    private val syncGradesUseCase: SyncGradesUseCase,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    private val maxCacheAge = 6.hours

    suspend operator fun invoke() {
        groupRepository.getAllIds().first()
            .mapNotNull { App.groupSource.getById(it).getFirstValue() }
            .forEach { group ->
                if (group.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                groupRepository.getById(group.id, true).getFirstValue()
            }

        roomRepository.getAllIds().first()
            .mapNotNull { App.roomSource.getById(it).getFirstValue() }
            .forEach { room ->
                if (room.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                roomRepository.getById(room.id, true).getFirstValue()
            }

        teacherRepository.getAllIds().first()
            .mapNotNull { App.teacherSource.getById(it).getFirstValue() }
            .forEach { teacher ->
                if (teacher.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                teacherRepository.getById(teacher.id, true).getFirstValue()
            }

        schoolRepository.getAllIds().first()
            .mapNotNull { App.schoolSource.getById(it).getFirstValue() }
            .forEach { school ->
                if (school.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                schoolRepository.getById(school.id, true).getFirstValue()
            }

        schoolRepository.getAll().first()
            .filterIsInstance<School.IndiwareSchool>()
            .filter { it.credentialsValid }
            .forEach { school ->
                when (checkSp24CredentialsUseCase(school.sp24Id.toInt(), school.username, school.password)) {
                    SchoolSettingsCredentialsState.Error -> return@forEach
                    SchoolSettingsCredentialsState.Invalid -> {
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
                updateDefaultLessonsUseCase(school)
                updateHolidaysUseCase(school)
                updateWeeksUseCase(school)
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
                updateSubstitutionPlanUseCase(school, today, true)
                updateSubstitutionPlanUseCase(school, nextDay, true)
            }

        updateHomeworkUseCase(true)
        updateAssessmentUseCase(true)

        fileRepository.getAllIds().first()
            .filter { it > 0 }
            .mapNotNull { App.fileSource.getById(it).getFirstValue() }
            .forEach { file ->
                if (file.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                fileRepository.getById(file.id, true).getFirstValue()
            }

        vppIdRepository.getAllIds().first()
            .mapNotNull { App.vppIdSource.getById(it).getFirstValue() }
            .forEach { vppId ->
                if (vppId.cachedAt.toLocalDateTime(TimeZone.currentSystemDefault()) until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) < maxCacheAge) return@forEach
                vppIdRepository.getById(vppId.id, true).getFirstValue()
            }

        syncGradesUseCase(true)
    }
}