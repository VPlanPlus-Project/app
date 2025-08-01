package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.model.schulverwalter.Interval

class GetIntervalsUseCase {
    operator fun invoke(): Flow<List<Interval>> {
        return App.intervalSource.getAll()
            .map { intervals ->
                intervals.filterIsInstance<CacheStateOld.Done<Interval>>().map { it.data }
                    .sortedBy { it.id }
            }
        }
}