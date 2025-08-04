package plus.vplan.app.feature.sync.domain.usecase.schulverwalter

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import plus.vplan.app.App
import plus.vplan.app.StartTaskJson
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.VppIdRepository
import plus.vplan.app.domain.repository.schulverwalter.CollectionRepository
import plus.vplan.app.domain.repository.schulverwalter.FinalGradeRepository
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository
import plus.vplan.app.domain.repository.schulverwalter.YearRepository
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.utils.now

class SyncGradesUseCase(
    private val gradeRepository: GradeRepository,
    private val yearRepository: YearRepository,
    private val intervalRepository: IntervalRepository,
    private val collectionRepository: CollectionRepository,
    private val finalGradeRepository: FinalGradeRepository,
    private val platformNotificationRepository: PlatformNotificationRepository,
    private val getGradeLockStateUseCase: GetGradeLockStateUseCase,
    private val schulverwalterRepository: SchulverwalterRepository,
    private val vppIdRepository: VppIdRepository
) {
    suspend operator fun invoke(allowNotifications: Boolean) {
        run schulverwalterConnectionRefresh@{
            val invalidVppIds = schulverwalterRepository.checkAccess()
            vppIdRepository
                .getVppIds().first().filterIsInstance<VppId.Active>()
                .filter { it.id in invalidVppIds }.forEach { vppId ->
                    vppIdRepository.getUserByToken(vppId.accessToken, true)
                }

            val invalidVppIdsAfterTokenReload = schulverwalterRepository.checkAccess()
            invalidVppIdsAfterTokenReload.forEach { stillInvalidVppId ->
                val vppId = App.vppIdSource.getById(stillInvalidVppId).getFirstValueOld() as? VppId.Active ?: return@forEach
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
                val vppId = App.vppIdSource.getById(nowValidVppId).getFirstValueOld() as? VppId.Active ?: return@forEach
                schulverwalterRepository.setSchulverwalterAccessValidity(vppId.schulverwalterConnection!!.accessToken, true)
            }
        }

        yearRepository.download()
        intervalRepository.download()
        collectionRepository.download()
        finalGradeRepository.download()
        val existingGrades = gradeRepository.getAllIds().first().toSet()
        val downloadedGrades = gradeRepository.download()

        if (allowNotifications && downloadedGrades is Response.Success && downloadedGrades.data.isNotEmpty()) {
            val newGradeIds = (downloadedGrades.data - existingGrades)
            val newGrades = combine(newGradeIds.ifEmpty { return }.map { ids -> App.gradeSource.getById(ids).filterIsInstance<CacheState.Done<Grade>>().map { it.data } }) { it.toList() }.first()
                .filter {  LocalDate.now().toEpochDays() - it.givenAt.toEpochDays() <= 2 }

            if (newGrades.isEmpty()) return
            if (newGrades.size == 1 && newGrades.first().value != null) {
                platformNotificationRepository.sendNotification(
                    title = "Neue Note",
                    category = newGrades.first().vppId.getFirstValueOld()?.name ?: "Unbekannter Nutzer",
                    message = buildString {
                        append("Du hast eine ")
                        if (getGradeLockStateUseCase().first().canAccess) append(newGrades.first().value)
                        else append("neue Note")
                        append(" in ")
                        append(newGrades.first().collection.getFirstValueOld()?.subject?.getFirstValueOld()?.name ?: "Unbekanntes Fach")
                        append(" für ")
                        append(newGrades.first().collection.getFirstValueOld()?.name)
                        append(" (")
                        append(newGrades.first().collection.getFirstValueOld()?.type)
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
                                            gradeId = newGrades.first().id
                                        )
                                    )
                                )
                            )
                        ).also { Logger.d { "Task: $it" } }
                    )
                )
            }
            if (newGrades.size > 1) {
                platformNotificationRepository.sendNotification(
                    title = "Neue Noten",
                    category = newGrades.first().vppId.getFirstValueOld()?.name ?: "Unbekannter Nutzer",
                    message = buildString {
                        append("Du hast ")
                        append(newGrades.filter { it.value != null }.size)
                        append(" neue Noten erhalten")
                    },
                    isLarge = false,
                    onClickData = Json.encodeToString(
                        StartTaskJson(
                            type = "navigate_to",
                            value = Json.encodeToString(
                                StartTaskJson.StartTaskNavigateTo(
                                    screen = "grades",
                                    value = Json.encodeToString(
                                        StartTaskJson.StartTaskNavigateTo.Grades(newGrades.first().vppIdId)
                                    )
                                )
                            )
                        )
                    ).also { Logger.d { "Task: $it" } }
                )
            }
        }
    }
}