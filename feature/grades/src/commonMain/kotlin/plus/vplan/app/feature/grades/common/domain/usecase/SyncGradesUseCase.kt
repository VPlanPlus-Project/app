package plus.vplan.app.feature.grades.common.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.besteschule.BesteSchuleRepository
import plus.vplan.app.core.data.besteschule.CollectionsRepository
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.data.besteschule.YearsRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.application.StartTaskJson
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.utils.date.atStartOfDay
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.until
import kotlin.time.Duration.Companion.days

class SyncGradesUseCase(
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val vppIdRepository: VppIdRepository
): KoinComponent {
    private val besteSchuleCollectionsRepository by inject<CollectionsRepository>()
    private val besteSchuleYearsRepository by inject<YearsRepository>()
    private val besteSchuleIntervalsRepository by inject<IntervalsRepository>()
    private val besteSchuleGradesRepository by inject<GradesRepository>()

    private val besteSchuleRepository by inject<BesteSchuleRepository>()

    private val profileRepository by inject<ProfileRepository>()
    private val platformNotificationRepository by inject<NotificationRepository>()
    private val analyticsRepository by inject<AnalyticsRepository>()

    suspend operator fun invoke(allowNotifications: Boolean, yearId: Int? = null) {
        try {
            run schulverwalterConnectionRefresh@{
                val invalidVppIds = vppIdRepository
                    .getVppIds().first()
                    .filterIsInstance<VppId.Active>()
                    .mapNotNull { vppId ->
                        val isValid = besteSchuleRepository.checkValidity(vppId.schulverwalterConnection!!.userId)
                        if (isValid) return@mapNotNull null
                        // fixme actually reload the user by re-fetching from network
                        return@mapNotNull vppId.id
                    }
                    .toSet()

                val invalidVppIdsAfterTokenReload = vppIdRepository
                    .getVppIds().first()
                    .filterIsInstance<VppId.Active>()
                    .mapNotNull { vppId ->
                        val isValid = besteSchuleRepository.checkValidity(vppId.schulverwalterConnection!!.userId)
                        if (isValid) return@mapNotNull null
                        return@mapNotNull vppId.id
                    }
                    .toSet()

                invalidVppIdsAfterTokenReload.forEach { stillInvalidVppId ->
                    val vppId = vppIdRepository.getById(stillInvalidVppId).first() as? VppId.Active ?: return@forEach
                    if (vppId.schulverwalterConnection!!.isValid == false) return@forEach
                    besteSchuleRepository.saveBesteSchuleAccessValidity(vppId.schulverwalterConnection!!.userId, false)
                    platformNotificationRepository.sendNotification(
                        title = "beste.schule-Zugang ungültig",
                        message = "Wir können keine Daten von beste.schule mehr abrufen. Tippe hier, um dich erneut in beste.schule anzumelden",
                        isLarge = false,
                        category = vppId.name,
                        onClickData = Json.encodeToString(
                            StartTaskJson(
                                type = "open",
                                value = Json.encodeToString(
                                    StartTaskJson.StartTaskOpen(
                                        type = "schulverwalter-reauth",
                                        value = Json.encodeToString(
                                            StartTaskJson.StartTaskOpen.SchulverwalterReauth(
                                                userId = vppId.id
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                }
                (invalidVppIds - invalidVppIdsAfterTokenReload).forEach { nowValidVppId ->
                    val vppId = vppIdRepository.getById(nowValidVppId).first() as? VppId.Active ?: return@forEach
                    besteSchuleRepository.saveBesteSchuleAccessValidity(vppId.schulverwalterConnection!!.userId, true)
                }
            }

            val vppIds = vppIdRepository.getVppIds().first()
                .filterIsInstance<VppId.Active>()
                .filter { it.schulverwalterConnection?.accessToken != null }

            val existingGradeIdsBeforeUpdate = besteSchuleGradesRepository.getAll().first()
                .map { grade -> grade.id }

            val years = besteSchuleYearsRepository
                .getAll(forceRefresh = true)
                .first()

            vppIds.forEach forEachUser@{ user ->
                val schulverwalterUserId = user.schulverwalterConnection!!.userId

                (if (yearId == null) years.map { it.id }
                else listOf(yearId)).forEach { year ->
                    val yearChangeSuccess = besteSchuleYearsRepository.setYear(
                        userId = schulverwalterUserId,
                        yearId = year
                    )

                    if (!yearChangeSuccess) {
                        Logger.e { "Failed to change year in beste.schule" }
                        return@forEachUser
                    }

                    besteSchuleIntervalsRepository
                        .getAll(forceRefresh = true)
                        .first()

                    besteSchuleCollectionsRepository
                        .getAll(forceRefresh = true)
                        .first()

                    besteSchuleGradesRepository.getAllForUser(
                        schulverwalterUserId = schulverwalterUserId,
                        forceRefresh = true,
                    ).first()

                }

                if (yearId != null) {
                    val yearChangeBackSuccess = besteSchuleYearsRepository.setYear(
                        userId = schulverwalterUserId,
                        yearId = null
                    )

                    if (!yearChangeBackSuccess) {
                        Logger.e { "Failed to change year in beste.schule" }
                        return@forEachUser
                    }
                }
            }

            val existingGradesAfterUpdate = besteSchuleGradesRepository.getAll().first()

            val gradesEligibleForNotification = existingGradesAfterUpdate
                .filter { it.id !in existingGradeIdsBeforeUpdate }
                .filter { it.value != null }
                .filter { it.givenAt.atStartOfDay() until LocalDate.now().atStartOfDay() <= 2.days }

            if (yearId == null && allowNotifications && gradesEligibleForNotification.isNotEmpty()) {
                if (gradesEligibleForNotification.size == 1) {
                    val newGrade = gradesEligibleForNotification.first()
                    val gradeReceiverVppId = profileRepository.getAll().first()
                        .filterIsInstance<Profile.StudentProfile>()
                        .mapNotNull { it.vppId }
                        .firstOrNull { it.schulverwalterConnection?.userId == newGrade.schulverwalterUserId }

                    platformNotificationRepository.sendNotification(
                        title = "Neue Note",
                        category = gradeReceiverVppId?.name ?: "Unbekannter Nutzer",
                        message = buildString {
                            append("Du hast eine ")
                            if (getGradeLockStateUseCase().first().canAccess) append(newGrade.value)
                            else append("neue Note")
                            append(" in ")
                            append(newGrade.collection.subject.fullName)
                            append(" für ")
                            append(newGrade.collection.name)
                            append(" (")
                            append(newGrade.collection.type)
                            append(") erhalten.")
                        },
                        isLarge = false,
                        onClickData = Json.encodeToString(
                            StartTaskJson(
                                type = "open",
                                value = Json.encodeToString(
                                    StartTaskJson.StartTaskOpen(
                                        type = "grade",
                                        value = Json.encodeToString(
                                            StartTaskJson.StartTaskOpen.Grade(
                                                gradeId = newGrade.id
                                            )
                                        )
                                    )
                                )
                            ).also { Logger.d { "Task: $it" } }
                        )
                    )
                } else if (gradesEligibleForNotification.size > 1) {
                    val schulverwalterUserIdsThatGotNewGrades = gradesEligibleForNotification
                        .map { it.schulverwalterUserId }
                        .toSet()

                    val gradeReceiverVppIds = profileRepository.getAll().first()
                        .filterIsInstance<Profile.StudentProfile>()
                        .mapNotNull { it.vppId }
                        .filter { it.schulverwalterConnection?.userId in schulverwalterUserIdsThatGotNewGrades }

                    platformNotificationRepository.sendNotification(
                        title = "Neue Noten",
                        category = when (gradeReceiverVppIds.size) {
                            0 -> "Unbekannte Nutzer"
                            1 -> gradeReceiverVppIds.first().name
                            else -> "Mehrere Nutzer"
                        },
                        message = buildString {
                            append("Du hast ")
                            append(gradesEligibleForNotification.size)
                            append(" neue Noten erhalten")
                        },
                        isLarge = false,
                        onClickData = if (gradeReceiverVppIds.isNotEmpty()) Json.encodeToString(
                            StartTaskJson(
                                type = "navigate_to",
                                value = Json.encodeToString(
                                    StartTaskJson.StartTaskNavigateTo(
                                        screen = "grades",
                                        value = Json.encodeToString(
                                            StartTaskJson.StartTaskNavigateTo.Grades(
                                                gradeReceiverVppIds.first().id
                                            )
                                        )
                                    )
                                )
                            )
                        ) else null
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e { "Failed to sync grades: ${e.stackTraceToString()}" }
            analyticsRepository.captureError("SyncGradesUseCase", e.stackTraceToString())
        }
    }
}