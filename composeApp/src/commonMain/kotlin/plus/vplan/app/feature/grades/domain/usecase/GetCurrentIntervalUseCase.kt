package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.utils.now

class GetCurrentIntervalUseCase {
    suspend operator fun invoke(): Interval? {
        return App.intervalSource.getAll()
            .map { intervalIds ->
                intervalIds.filterIsInstance<CacheState.Done<Interval>>().map { it.data }
            }
            .first()
            .firstOrNull { LocalDate.now() in it.from..it.to }
    }
}