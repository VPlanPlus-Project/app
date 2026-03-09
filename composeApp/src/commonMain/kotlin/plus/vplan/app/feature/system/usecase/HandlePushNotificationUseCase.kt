package plus.vplan.app.feature.system.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.homework.HomeworkRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.school.SchoolRepository
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.feature.sync.domain.usecase.vpp.UpdateAssessmentsUseCase

class HandlePushNotificationUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val updateAssessmentsUseCase: UpdateAssessmentsUseCase,
    private val schoolRepository: SchoolRepository,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val analyticsRepository: AnalyticsRepository,
    private val profileRepository: ProfileRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = Logger.withTag("HandlePushNotificationUseCase")

    suspend operator fun invoke(topic: String, payload: String) {
        when (topic) {
            "HOMEWORK_UPDATE" -> {
                val data = json.decodeFromString<HomeworkUpdate>(payload)
                // Sync homework when push notification is received
                data.homeworkIds.forEach {
                    profileRepository.getAll().first()
                        .filterIsInstance<Profile.StudentProfile>()
                        .mapNotNull { it.vppId }
                        .forEach { vppId ->
                            homeworkRepository.syncById(vppId, it, forceReload = true)
                        }
                }
            }
            "ASSESSMENT_UPDATE" -> {
                val data = json.decodeFromString<AssessmentUpdate>(payload)
                // Sync assessments when push notification is received
                updateAssessmentsUseCase(allowNotifications = false)
            }
            "INDIWARE_UPDATE" -> {
                val data = json.decodeFromString<IndiwareUpdate>(payload)
                val school = schoolRepository
                    .getById(Alias(AliasProvider.Sp24, data.indiwareSchoolId, 1))
                    .first() as? School.AppSchool

                if (school == null) {
                    logger.w { "Indiware school ${data.indiwareSchoolId} not found" }
                    analyticsRepository.capture("PushHandler.IndiwareSchoolNotFound", mapOf("indiware_id" to data.indiwareSchoolId))
                    return
                }

                val dates = data.dates.mapNotNull {
                    try {
                        LocalDate.parse(it)
                    } catch (_: IllegalArgumentException) {
                        analyticsRepository.capture("PushHandler.DateParseError", mapOf("value" to it))
                        null
                    }
                }
                updateSubstitutionPlanUseCase(
                    sp24School = school,
                    dates = dates,
                    allowNotification = true
                )

                if (data.timetable) {
                    updateTimetableUseCase(
                        sp24School = school,
                        forceUpdate = true,
                    )
                }
            }
        }
    }
}

@Serializable
private data class HomeworkUpdate(
    @SerialName("homework_ids") val homeworkIds: List<Int>
)

@Serializable
private data class AssessmentUpdate(
    @SerialName("assessment_ids") val assessmentIds: List<Int>
)

@Serializable
private data class IndiwareUpdate(
    @SerialName("indiware_school_id") val indiwareSchoolId: String,
    @SerialName("dates") val dates: List<String>,
    @SerialName("timetable") val timetable: Boolean
)