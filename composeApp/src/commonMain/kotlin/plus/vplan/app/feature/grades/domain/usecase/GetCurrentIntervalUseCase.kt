package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.utils.now

class GetCurrentIntervalUseCase {
    suspend operator fun invoke(schulverwalterUserId: Int): Interval? {
        return App.intervalSource.getForUser(schulverwalterUserId)
            .first()
            .sortedBy { it.collectionIds.size }
            .firstOrNull { LocalDate.now() in it.from..it.to }
    }
}