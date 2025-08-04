package plus.vplan.app.feature.system.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.capture
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase

class HandlePushNotificationUseCase(
    private val schoolRepository: SchoolRepository,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val updateTimetableUseCase: UpdateTimetableUseCase
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = Logger.withTag("HandlePushNotificationUseCase")

    suspend operator fun invoke(topic: String, payload: String) {
        when (topic) {
            "HOMEWORK_UPDATE" -> {
                val data = json.decodeFromString<HomeworkUpdate>(payload)
                data.homeworkIds.forEach {
                    App.homeworkSource.getById(it, forceUpdate = true).getFirstValueOld()
                }
            }
            "ASSESSMENT_UPDATE" -> {
                val data = json.decodeFromString<AssessmentUpdate>(payload)
                data.assessmentIds.forEach {
                    App.assessmentSource.getById(it, forceUpdate = true).getFirstValueOld()
                }
            }
            "INDIWARE_UPDATE" -> {
                val data = json.decodeFromString<IndiwareUpdate>(payload)
                val school = schoolRepository.getAllLocalIds().first()
                    .map { schoolRepository.getByLocalId(it).first() }
                    .filterIsInstance<School.AppSchool>()
                    .firstOrNull { it.sp24Id == data.indiwareSchoolId }

                if (school == null) {
                    logger.w { "Indiware school ${data.indiwareSchoolId} not found" }
                    capture("PushHandler.IndiwareSchoolNotFound", mapOf("indiware_id" to data.indiwareSchoolId))
                    return
                }

                val dates = data.dates.mapNotNull {
                    try {
                        LocalDate.parse(it)
                    } catch (_: IllegalArgumentException) {
                        capture("PushHandler.DateParseError", mapOf("value" to it))
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