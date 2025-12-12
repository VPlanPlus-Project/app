package plus.vplan.app.domain.model.besteschule

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import kotlin.time.Instant

@Stable
data class BesteSchuleYear(
    val id: Int,
    val name: String,
    val from: LocalDate,
    val to: LocalDate,
    val cachedAt: Instant,
    val intervalIds: Set<Int>
): KoinComponent {
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()

    val intervals by lazy {
        combine(this.intervalIds.map { intervalId ->
            besteSchuleIntervalsRepository.getIntervalFromCache(intervalId)
        }) { it.filterNotNull() }
    }
}