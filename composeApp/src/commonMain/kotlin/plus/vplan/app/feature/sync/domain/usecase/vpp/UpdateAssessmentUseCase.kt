package plus.vplan.app.feature.sync.domain.usecase.vpp

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.feature.profile.domain.usecase.UpdateAssessmentIndicesUseCase

class UpdateAssessmentUseCase(
    private val assessmentRepository: AssessmentRepository,
    private val profileRepository: ProfileRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val updateAssessmentIndicesUseCase: UpdateAssessmentIndicesUseCase
) {
    private val logger = Logger.withTag("UpdateAssessmentUseCase")

    suspend operator fun invoke(allowNotifications: Boolean) {
        val existing = assessmentRepository.getAll().first().map { it.id }.toSet()
        val profiles = profileRepository.getAll().first().filterIsInstance<Profile.StudentProfile>()
        profiles.forEach forEachProfile@{ profile ->
            val apiAuthentication = profile.getVppIdItem()?.buildVppSchoolAuthentication() ?: profile.getSchool().getFirstValue()?.buildSp24AppAuthentication()
            if (apiAuthentication == null) {
                logger.e { "No api authentication found for profile ${profile.id} (${profile.name})" }
                return@forEachProfile
            }
            return@forEachProfile // TODO: Implement assessment download logic
//            val apiResponse = assessmentRepository.download(apiAuthentication, TODO())
//            if (apiResponse !is Response.Success) {
//                logger.w { "Error downloading assessments for profile ${profile.id} (${profile.name}): $apiResponse" }
//                return@forEachProfile
//            }
//            (apiResponse.data - existing)
//                .also { ids ->
//                    if (ids.isEmpty() || !allowNotifications) return@forEachProfile
//                    combine(ids.map { assessmentId -> App.assessmentSource.getById(assessmentId).filterIsInstance<CacheStateOld.Done<Assessment>>().map { it.data } }) { it.toList() }.first()
//                        .filter { it.creator is AppEntity.VppId && it.creator.id != profile.vppIdId && (it.createdAt until Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) < 2.days }
//                        .filter { it.subjectInstance.getFirstValueOld()!!.id in profile.subjectInstanceConfiguration.filterValues { it }.keys }
//                        .let { newAssessments ->
//                            if (newAssessments.isEmpty()) return@forEachProfile
//                            if (newAssessments.size == 1) {
//                                val message =  buildString {
//                                    newAssessments.first().let { assessment ->
//                                        append((assessment.creator as AppEntity.VppId).vppId.getFirstValueOld()?.name ?: "Unbekannter Nutzer")
//                                        append(" hat eine neue Leistungserhebung in ")
//                                        append(assessment.subjectInstance.getFirstValueOld()?.subject ?: "einem Fach")
//                                        append(" für ")
//                                        (assessment.date untilRelativeText LocalDate.now())?.let { append(it) } ?: append(assessment.date.format(LocalDate.Format {
//                                            dayOfWeek(shortDayOfWeekNames)
//                                            chars(", ")
//                                            dayOfMonth()
//                                            chars(". ")
//                                            monthName(shortMonthNames)
//                                        }))
//                                        append(" erstellt.")
//                                    }
//                                }
//                                platformNotificationRepository.sendNotification(
//                                    title = "Neue Leistungserhebung",
//                                    category = profile.name,
//                                    message = message,
//                                    largeText = "$message\n${newAssessments.first().description}",
//                                    isLarge = true,
//                                    onClickData = Json.encodeToString(
//                                        StartTaskJson(
//                                            type = "open",
//                                            profileId = profile.id.toString(),
//                                            value = Json.encodeToString(
//                                                StartTaskJson.StartTaskOpen(
//                                                    type = "assessment",
//                                                    value = Json.encodeToString(
//                                                        StartTaskJson.StartTaskOpen.Assessment(
//                                                            assessmentId = newAssessments.first().id
//                                                        )
//                                                    )
//                                                )
//                                            )
//                                        ).also { Logger.d { "Task: $it" } }
//                                    )
//                                )
//                            }
//                            else platformNotificationRepository.sendNotification(
//                                title = "Neue Leistungserhebungen",
//                                category = profile.name,
//                                message = buildString {
//                                    append("Es gibt ${newAssessments.size} neue Leistungserhebungen für dich")
//                                },
//                                isLarge = false,
//                            )
//                        }
//                }
        }

        profiles.forEach {
            updateAssessmentIndicesUseCase(it)
        }
    }
}