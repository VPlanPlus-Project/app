package plus.vplan.app.feature.sync.domain.usecase.besteschule

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleYearsRepository
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.utils.atStartOfDay
import plus.vplan.app.utils.now
import plus.vplan.app.utils.until
import kotlin.time.Duration.Companion.days

class SyncGradesUseCase(
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val schulverwalterRepository: SchulverwalterRepository,
    private val vppIdRepository: VppIdRepository
): KoinComponent {
    private val besteSchuleTeachersRepository by inject<BesteSchuleTeachersRepository>()
    private val besteSchuleCollectionsRepository by inject<BesteSchuleCollectionsRepository>()
    private val besteSchuleYearsRepository by inject<BesteSchuleYearsRepository>()
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()
    private val besteSchuleApiRepository by inject<BesteSchuleApiRepository>()

    private val profileRepository by inject<ProfileRepository>()
    private val platformNotificationRepository by inject<PlatformNotificationRepository>()

    suspend operator fun invoke(allowNotifications: Boolean, yearId: Int? = null) {
        run schulverwalterConnectionRefresh@{
            val invalidVppIds = schulverwalterRepository.checkAccess()
            vppIdRepository
                .getVppIds().first().filterIsInstance<VppId.Active>()
                .filter { it.id in invalidVppIds }.forEach { vppId ->
                    vppIdRepository.getUserByToken(vppId.accessToken)
                    // fixme actually reload the user
                }

            val invalidVppIdsAfterTokenReload = schulverwalterRepository.checkAccess()
            invalidVppIdsAfterTokenReload.forEach { stillInvalidVppId ->
                val vppId = vppIdRepository.getById(stillInvalidVppId, ResponsePreference.Fast).getFirstValueOld() as? VppId.Active ?: return@forEach
                if (vppId.schulverwalterConnection!!.isValid == false) return@forEach
                schulverwalterRepository.setSchulverwalterAccessValidity(vppId.schulverwalterConnection.accessToken, false)
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
                val vppId = vppIdRepository.getById(nowValidVppId, ResponsePreference.Fast).getFirstValueOld() as? VppId.Active ?: return@forEach
                schulverwalterRepository.setSchulverwalterAccessValidity(vppId.schulverwalterConnection!!.accessToken, true)
            }
        }

        val vppIds = vppIdRepository.getVppIds().first()
            .filterIsInstance<VppId.Active>()
            .filter { it.schulverwalterConnection?.accessToken != null }

        val existingGradeIdsBeforeUpdate = getCachedGrades().map { grade -> grade.id }

        vppIds.forEach forEachUser@{ user ->
            val schulverwalterUserId = user.schulverwalterConnection!!.userId
            val schulverwalterAccessToken = user.schulverwalterConnection.accessToken

            val yearsResponse = besteSchuleYearsRepository
                .getYears(
                    responsePreference = ResponsePreference.Fresh,
                    contextBesteschuleAccessToken = schulverwalterAccessToken
                )
                .first()
            if (yearsResponse is Response.Error) {
                Logger.e { "Failed to get years from beste.schule: $yearsResponse" }
                return@forEachUser
            }

            (if (yearId == null) (yearsResponse as Response.Success).data.map { it.id }
                else listOf(yearId)).forEach { year ->
                besteSchuleApiRepository.clearApiCache()
                val yearChangeError = besteSchuleApiRepository.setYearForUser(
                    schulverwalterAccessToken = schulverwalterAccessToken,
                    yearId = year
                ) as? Response.Error

                if (yearChangeError != null) {
                    Logger.e { "Failed to change year in beste.schule: $yearChangeError" }
                    return@forEachUser
                }

                val intervalError = besteSchuleIntervalsRepository.getIntervals(
                    responsePreference = ResponsePreference.Fresh,
                    contextBesteschuleAccessToken = schulverwalterAccessToken,
                    contextBesteschuleUserId = schulverwalterUserId,
                    withCache = yearId != null
                ).first() as? Response.Error
                if (intervalError != null) {
                    Logger.e { "Failed to get intervals from beste.schule: $intervalError" }
                    return@forEachUser
                }

                val teacherError = besteSchuleTeachersRepository
                    .getTeachers(
                        responsePreference = ResponsePreference.Fresh,
                        contextBesteschuleAccessToken = schulverwalterAccessToken,
                    ).first() as? Response.Error
                if (teacherError != null) {
                    Logger.e { "Failed to get teachers from beste.schule: $teacherError" }
                    return@forEachUser
                }

                val collectionsError = besteSchuleCollectionsRepository.getCollections(
                    responsePreference = ResponsePreference.Fresh,
                    contextBesteschuleAccessToken = schulverwalterAccessToken,
                ).first() as? Response.Error

                if (collectionsError != null) {
                    Logger.e { "Failed to get collections from beste.schule: $collectionsError" }
                    return@forEachUser
                }

                val subjectError = besteSchuleSubjectsRepository.getSubjects(
                    responsePreference = ResponsePreference.Fresh,
                    contextBesteschuleAccessToken = schulverwalterAccessToken,
                    contextBesteschuleUserId = schulverwalterUserId
                ).first() as? Response.Error
                if (subjectError != null) {
                    Logger.e { "Failed to get subjects from beste.schule: $subjectError" }
                    return@forEachUser
                }

                val gradesError = besteSchuleGradesRepository.getGrades(
                    responsePreference = ResponsePreference.Fresh,
                    contextBesteschuleAccessToken = schulverwalterAccessToken,
                    contextBesteschuleUserId = schulverwalterUserId
                ).first() as? Response.Error
                if (gradesError != null) {
                    Logger.e { "Failed to get grades from beste.schule: $gradesError" }
                    return@forEachUser
                }
            }

            if (yearId != null) {
                val yearChangeBackError = besteSchuleApiRepository.setYearForUser(
                    schulverwalterAccessToken = schulverwalterAccessToken,
                    yearId = null
                ) as? Response.Error

                if (yearChangeBackError != null) {
                    Logger.e { "Failed to change year in beste.schule: $yearChangeBackError" }
                    return@forEachUser
                }
            }
        }

        val existingGradesAfterUpdate = getCachedGrades()

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

                val collection = newGrade.collection.first()!!
                val subject = collection.subject.first()

                platformNotificationRepository.sendNotification(
                    title = "Neue Note",
                    category = gradeReceiverVppId?.name ?: "Unbekannter Nutzer",
                    message = buildString {
                        append("Du hast eine ")
                        if (getGradeLockStateUseCase().first().canAccess) append(newGrade.value)
                        else append("neue Note")
                        append(" in ")
                        append(subject?.fullName ?: "Unbekanntes Fach")
                        append(" für ")
                        append(collection.name)
                        append(" (")
                        append(collection.type)
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
                val schulverwalterUserIdsThatGotNewGrades = gradesEligibleForNotification.map { it.schulverwalterUserId }.toSet()
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
                                        StartTaskJson.StartTaskNavigateTo.Grades(gradeReceiverVppIds.first().id)
                                    )
                                )
                            )
                        )
                    ) else null
                )
            }
        }
    }

    private suspend fun getCachedGrades() = besteSchuleGradesRepository.getGrades(
        responsePreference = ResponsePreference.Fast,
        contextBesteschuleUserId = null,
        contextBesteschuleAccessToken = null
    )
        .filterIsInstance<Response.Success<List<BesteSchuleGrade>>>()
        .map { it.data }
        .first()
        .toSet()
}