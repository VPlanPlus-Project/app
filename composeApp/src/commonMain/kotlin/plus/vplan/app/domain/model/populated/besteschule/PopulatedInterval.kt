@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated.besteschule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval

data class PopulatedInterval(
    val interval: BesteSchuleInterval,
    val includedInterval: BesteSchuleInterval?
)

class IntervalPopulator: KoinComponent {
    private val besteSchuleIntervalsRepository by inject<IntervalsRepository>()

    fun populateMultiple(intervals: List<BesteSchuleInterval>): Flow<List<PopulatedInterval>> {
        val intervalsFlow = besteSchuleIntervalsRepository.getAll()

        return intervalsFlow.mapLatest { intervalItems ->
            intervals.mapNotNull{ interval ->
                PopulatedInterval(
                    interval = interval,
                    includedInterval = interval.includedIntervalId?.let { intervalItems.firstOrNull { item -> item.id == it } ?: return@mapNotNull null }
                )
            }
        }
    }

    fun populateSingle(interval: BesteSchuleInterval): Flow<PopulatedInterval> {
        val includedInterval = interval.includedIntervalId?.let {
            besteSchuleIntervalsRepository.getById(it)
        } ?: flowOf(null)

        return includedInterval.mapLatest { includedInterval ->
            PopulatedInterval(
                interval = interval,
                includedInterval = includedInterval
            )
        }
    }
}