package plus.vplan.app.domain.model.populated.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.YearsRepository
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.model.besteschule.BesteSchuleYear
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository

data class PopulatedInterval(
    val interval: BesteSchuleInterval,
    val year: BesteSchuleYear,
    val includedInterval: BesteSchuleInterval?
)

class IntervalPopulator: KoinComponent {

    private val besteSchuleYearsRepository by inject<YearsRepository>()
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()

    fun populateMultiple(intervals: List<BesteSchuleInterval>): Flow<List<PopulatedInterval>> {
        val years = besteSchuleYearsRepository.getAll()
        val intervalsFlow = besteSchuleIntervalsRepository.getAllFromCache()

        return combine(years, intervalsFlow) { years, intervalItems ->
            intervals.mapNotNull{ interval ->
                PopulatedInterval(
                    interval = interval,
                    year = years.firstOrNull { it.id == interval.yearId } ?: return@mapNotNull null,
                    includedInterval = interval.includedIntervalId?.let { intervalItems.firstOrNull { item -> item.id == it } ?: return@mapNotNull null }
                )
            }
        }
    }

    fun populateSingle(interval: BesteSchuleInterval): Flow<PopulatedInterval> {
        val year = besteSchuleYearsRepository
            .getById(interval.yearId)
            .filterNotNull()
        val includedInterval = interval.includedIntervalId?.let {
            besteSchuleIntervalsRepository.getIntervalFromCache(it)
        } ?: flowOf(null)

        return combine(
            year,
            includedInterval
        ) { year, includedInterval ->
            PopulatedInterval(
                interval = interval,
                year = year,
                includedInterval = includedInterval
            )
        }
    }
}