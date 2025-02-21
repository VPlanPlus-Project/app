package plus.vplan.app.feature.sync.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.settings.page.school.domain.usecase.CheckSp24CredentialsUseCase
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateDefaultLessonsUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateHolidaysUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateHomeworkUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

class FullSyncUseCase(
    private val schoolRepository: SchoolRepository,
    private val dayRepository: DayRepository,
    private val updateHolidaysUseCase: UpdateHolidaysUseCase,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateDefaultLessonsUseCase: UpdateDefaultLessonsUseCase,
    private val updateHomeworkUseCase: UpdateHomeworkUseCase,
    private val updateAssessmentUseCase: UpdateAssessmentUseCase,
    private val checkSp24CredentialsUseCase: CheckSp24CredentialsUseCase,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    suspend operator fun invoke() {
        schoolRepository.getAll().first().filterIsInstance<School.IndiwareSchool>().forEach { school ->
            if (!school.credentialsValid) return@forEach

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

            updateSubstitutionPlanUseCase(school, today, true)
            updateSubstitutionPlanUseCase(school, nextDay, true)

        }
        updateHomeworkUseCase(true)
        updateAssessmentUseCase(true)
    }
}