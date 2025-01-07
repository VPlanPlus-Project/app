package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.WeekRepository

class WeekSource(
    private val weekRepository: WeekRepository
) : CacheableItemSource<Week>() {
    override fun getAll(configuration: FetchConfiguration<Week>): Flow<List<Cacheable<Week>>> {
        TODO("Not yet implemented")
    }

    override fun getById(id: String, configuration: FetchConfiguration<Week>): Flow<Cacheable<Week>> = channelFlow {
        weekRepository.getById(id).collectLatest { cachedWeek ->
            if (cachedWeek == null) return@collectLatest send(Cacheable.NotExisting(id))

            val week = MutableStateFlow(cachedWeek)
            launch { week.collectLatest { send(Cacheable.Loaded(it)) } }

            if (configuration is Week.Fetch) {
                if (configuration.school is School.Fetch) launch {
                    App.schoolSource.getById(cachedWeek.school.getItemId(), configuration.school).collectLatest {
                        week.value = week.value.copy(school = it)
                    }
                }
            }
        }
    }
}