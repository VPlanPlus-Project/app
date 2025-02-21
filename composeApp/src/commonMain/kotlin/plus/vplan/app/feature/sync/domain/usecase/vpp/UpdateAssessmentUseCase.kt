package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Duration.Companion.days

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val profileRepository: ProfileRepository,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    suspend operator fun invoke(allowNotifications: Boolean) {
        val existing = assessmentRepository.getAll().first().map { it.id }.toSet()
        profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>().forEach { profile ->
            ((assessmentRepository.download(profile.getVppIdItem()?.buildSchoolApiAccess() ?: profile.getSchoolItem().getSchoolApiAccess()!!, profile.defaultLessons.filterValues { it }.keys.toList()) as? Response.Success ?: return@forEach)
                .data - existing)
                .also { ids ->
                    if (ids.isEmpty() || !allowNotifications) return@forEach
                    combine(ids.map { App.assessmentSource.getById(it).filterIsInstance<CacheState.Done<Assessment>>().map { it.data } }) { it.toList() }.first()
                        .filter { it.creator is AppEntity.VppId && it.creator.id != profile.vppId /*&& (it.createdAt until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) < 2.days */}
                        .let { newAssessments ->
                            if (newAssessments.isEmpty()) return@forEach
                            if (newAssessments.size == 1) {
                                val message =  buildString {
                                    newAssessments.first().let { assessment ->
                                        append((assessment.creator as AppEntity.VppId).getVppIdItem()?.name ?: "Unbekannter Nutzer")
                                        append(" hat eine neue Leistungserhebung in ")
                                        append(assessment.getSubjectInstanceItem().subject)
                                        append(" für ")
                                        (assessment.date untilRelativeText LocalDate.now())?.let { append(it) } ?: append(assessment.date.format(LocalDate.Format {
                                            dayOfWeek(shortDayOfWeekNames)
                                            chars(", ")
                                            dayOfMonth()
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
    }
}