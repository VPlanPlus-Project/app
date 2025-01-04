package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.repository.HomeworkRepository

class HomeworkSource(
    private val homeworkRepository: HomeworkRepository
): CacheableItemSource<Homework> {
    override fun getAll(configuration: CacheableItemSource.FetchConfiguration<Homework>): Flow<List<Cacheable<Homework>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: CacheableItemSource.FetchConfiguration<Homework>
    ): Flow<Cacheable<Homework>> = channelFlow {
        homeworkRepository.getById(id.toInt()).collectLatest { cacheableHomework ->
            send(cacheableHomework)
            if (configuration is CacheableItemSource.FetchConfiguration.Ignore || cacheableHomework !is Cacheable.Loaded) return@collectLatest
            if (cacheableHomework.value is Homework.CloudHomework && configuration is Homework.Fetch) {
                if (configuration.vppId is CacheableItemSource.FetchConfiguration.Fetch) {
                    App.vppIdSource.getById(cacheableHomework.value.createdBy.getItemId(), configuration.vppId).collect { cacheableVppId ->
                        send(Cacheable.Loaded(cacheableHomework.value.copy(createdBy = cacheableVppId)))
                    }
                }
            }
        }
    }
}