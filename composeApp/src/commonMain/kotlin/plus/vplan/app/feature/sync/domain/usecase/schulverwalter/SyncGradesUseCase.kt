package plus.vplan.app.feature.sync.domain.usecase.schulverwalter

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.domain.repository.schulverwalter.CollectionRepository
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository
import plus.vplan.app.domain.repository.schulverwalter.YearRepository
import plus.vplan.app.utils.now

class SyncGradesUseCase(
    private val gradeRepository: GradeRepository,
    private val yearRepository: YearRepository,
    private val intervalRepository: IntervalRepository,
    private val collectionRepository: CollectionRepository,
    private val platformNotificationRepository: PlatformNotificationRepository
) {
    suspend operator fun invoke(allowNotifications: Boolean) {
        yearRepository.download()
        intervalRepository.download()
        collectionRepository.download()
        val existingGrades = gradeRepository.getAllIds().first().toSet()
        val downloadedGrades = gradeRepository.download()

        if (allowNotifications && downloadedGrades is Response.Success && downloadedGrades.data.isNotEmpty()) {
            val newGrades = combine((downloadedGrades.data - existingGrades).map { ids -> App.gradeSource.getById(ids).filterIsInstance<CacheState.Done<Grade>>().map { it.data } }) { it.toList() }.first()
                .filter {  LocalDate.now().toEpochDays() - it.givenAt.toEpochDays() <= 2 }

            if (newGrades.isEmpty()) return
            if (newGrades.size == 1) {
                platformNotificationRepository.sendNotification(
                    title = "Neue Note",
                    category = newGrades.first().vppId.getFirstValue()?.name ?: "Unbekannter Nutzer",
                    message = buildString {
                        append("Du hast eine ")
                        append(newGrades.first().value)
                        append(" in ")
                        append(newGrades.first().collection.getFirstValue()?.subject?.getFirstValue()?.name ?: "Unbekanntes Fach")
                        append(" für ")
                        append(newGrades.first().collection.getFirstValue()?.name)
                        append(" (")
                        append(newGrades.first().collection.getFirstValue()?.type)
                        append(") erhalten.")
                    },
                    isLarge = false,
                    onClickData = null
                )
            }
            if (newGrades.size > 1) {
                platformNotificationRepository.sendNotification(
                    title = "Neue Noten",
                    category = newGrades.first().vppId.getFirstValue()?.name ?: "Unbekannter Nutzer",
                    message = buildString {
                        append("Du hast ")
                        append(newGrades.size)
                        append(" neue Noten erhalten")
                    },
                    isLarge = false,
                    onClickData = null
                )
            }
        }
    }
}