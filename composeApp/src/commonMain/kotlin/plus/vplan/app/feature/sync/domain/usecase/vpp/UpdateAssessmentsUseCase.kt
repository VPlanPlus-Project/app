package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateAssessmentIndicesUseCase
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class UpdateAssessmentsUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val profileRepository: ProfileRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateAssessmentIndicesUseCase: UpdateAssessmentIndicesUseCase
) {
    private val logger = Logger.withTag("UpdateAssessmentUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val existing = assessmentRepository.getAll().first().map { it.id }.toSet()
        val profiles = profileRepository.getAll().first()
            .filterIsInstance<Profile.StudentProfile>()

        profiles.forEach forEachProfile@{ profile ->
            val apiAuthentication = profile.vppId?.buildVppSchoolAuthentication() ?: profile.school.buildSp24AppAuthentication()

            val enabledSubjectInstanceIds = profile.subjectInstanceConfiguration.filterValues { it }.keys
            val subjectInstanceAliases = enabledSubjectInstanceIds
                .mapNotNull { subjectInstanceRepository.getByLocalId(it).first() }
                .flatMap { it.aliases }

            val apiResponse = assessmentRepository.download(apiAuthentication, subjectInstanceAliases)
            if (apiResponse !is Response.Success) {
                logger.w { "Error downloading assessments for profile ${profile.id} (${profile.name}): $apiResponse" }
                return@forEachProfile
            }
            (apiResponse.data - existing)
                .also { ids ->
                    if (ids.isEmpty() || !allowNotifications) return@forEachProfile
                    combine(ids.map { assessmentId -> App.assessmentSource.getById(assessmentId).filterIsInstance<CacheState.Done<Assessment>>().map { it.data } }) { it.toList() }.first()
                        .filter { assessment -> assessment.creator is AppEntity.VppId && assessment.creator.id != profile.vppId?.id && (assessment.createdAt until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) < 2.days }
                        .filter { assessment -> assessment.subjectInstance.getFirstValue()!!.id in profile.subjectInstanceConfiguration.filterValues { it }.keys }
                        .let { newAssessments ->
                            if (newAssessments.isEmpty()) return@forEachProfile
                            if (newAssessments.size == 1) {
                                val message =  buildString {
                                    newAssessments.first().let { assessment ->
                                        append((assessment.creator as AppEntity.VppId).vppId.getFirstValueOld()?.name ?: "Unbekannter Nutzer")
                                        append(" hat eine neue Leistungserhebung in ")
                                        append(assessment.subjectInstance.getFirstValue()?.subject ?: "einem Fach")
                                        append(" für ")
                                        (assessment.date untilRelativeText LocalDate.now())?.let { append(it) } ?: append(assessment.date.format(LocalDate.Format {
                                            dayOfWeek(shortDayOfWeekNames)
                                            chars(", ")
                                            day(padding = Padding.ZERO)
                                            chars(". ")
                                            monthName(shortMonthNames)
                                        }))
                                        append(" erstellt.")
                                    }
                                }
                                platformNotificationRepository.sendNotification(
                                    title = "Neue Leistungserhebung",
                                    category = profile.name,
                                    message = message,
                                    largeText = "$message\n${newAssessments.first().description}",
                                    isLarge = true,
                                    onClickData = Json.encodeToString(
                                        StartTaskJson(
                                            type = "open",
                                            profileId = profile.id.toString(),
                                            value = Json.encodeToString(
                                                StartTaskJson.StartTaskOpen(
                                                    type = "assessment",
                                                    value = Json.encodeToString(
                                                        StartTaskJson.StartTaskOpen.Assessment(
                                                            assessmentId = newAssessments.first().id
                                                        )
                                                    )
                                                )
                                            )
                                        ).also { Logger.d { "Task: $it" } }
                                    )
                                )
                            }
                            else platformNotificationRepository.sendNotification(
                                title = "Neue Leistungserhebungen",
                                category = profile.name,
                                message = buildString {
                                    append("Es gibt ${newAssessments.size} neue Leistungserhebungen für dich")
                                },
                                isLarge = false,
                            )
                        }
                }
        }

        profiles.forEach {
            updateAssessmentIndicesUseCase(it)
        }
    }
}