package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import plus.vplan.app.core.data.assessment.AssessmentRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.application.StartTaskJson
import plus.vplan.app.core.model.getByProvider
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.shortDayOfWeekNames
import plus.vplan.app.core.utils.date.shortMonthNames
import plus.vplan.app.core.utils.date.until
import plus.vplan.app.core.utils.date.untilRelativeText
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileAssessmentIndexUseCase
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class UpdateAssessmentsUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val profileRepository: ProfileRepository,
    private val platformNotificationRepository: NotificationRepository,
    private val updateProfileAssessmentIndexUseCase: UpdateProfileAssessmentIndexUseCase,
) {
    private val logger = Logger.withTag("UpdateAssessmentUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val profiles = profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>()
        val existingAssessments = assessmentRepository.getAll().first()
        
        profiles.forEach forEachProfile@{ studentProfile ->
            logger.d { "Syncing assessments for ${studentProfile.name}" }
            
            studentProfile.school.aliases.getByProvider(AliasProvider.Vpp)?.value?.toInt() ?: run {
                logger.e { "No vpp provider for school ${studentProfile.school}" }
                return@forEachProfile
            }

            val enabledSubjectInstanceIds = studentProfile.subjectInstanceConfiguration.filterValues { it }.keys
            val subjectInstanceAliases = enabledSubjectInstanceIds.flatMap { it.aliases }

            // Sync using the repository with proper parameters
            val schoolApiAccess = studentProfile.vppId?.buildVppSchoolAuthentication() 
                ?: studentProfile.school.buildSp24AppAuthentication()
            assessmentRepository.sync(schoolApiAccess, subjectInstanceAliases)
            
            updateProfileAssessmentIndexUseCase(studentProfile)

            logger.d { "Checking if notifications should be sent" }
            run buildAndSendNotifications@{
                if (!allowNotifications) return@buildAndSendNotifications
                
                logger.d { "Checking if new assessments are created" }
                val allowedSubjectInstances = studentProfile.subjectInstanceConfiguration.keys
                
                val newAssessments = assessmentRepository.getAll().first()
                    .filter { assessment -> assessment.id !in existingAssessments.map { it.id } }
                    .filter { assessment ->
                        assessment.creator is AppEntity.VppId && 
                        (assessment.creator as AppEntity.VppId).vppId.id != studentProfile.vppId?.id && 
                        (assessment.createdAt until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) < 2.days
                    }
                    .filter { assessment -> allowedSubjectInstances.contains(assessment.subjectInstance) }

                if (newAssessments.isEmpty()) return@buildAndSendNotifications

                if (newAssessments.size == 1) {
                    val message = buildString {
                        newAssessments.first().let { assessment ->
                            append((assessment.creator as AppEntity.VppId).vppId.name)
                            append(" hat eine neue Leistungserhebung in ")
                            append(assessment.subjectInstance.subject)
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
                        category = studentProfile.name,
                        message = message,
                        largeText = "$message\n${newAssessments.first().description}",
                        isLarge = true,
                        onClickData = Json.encodeToString(
                            StartTaskJson(
                                type = "open",
                                profileId = studentProfile.id.toString(),
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
                } else {
                    platformNotificationRepository.sendNotification(
                        title = "Neue Leistungserhebungen",
                        category = studentProfile.name,
                        message = buildString {
                            append("Es gibt ${newAssessments.size} neue Leistungserhebungen für dich")
                        },
                        isLarge = false,
                    )
                }
            }
        }
        logger.i { "Done" }
    }
}
