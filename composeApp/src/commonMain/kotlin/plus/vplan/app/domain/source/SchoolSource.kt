package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository

class SchoolSource(
    private val schoolRepository: SchoolRepository
) : CacheableItemSource<School> {
    override fun getAll(configuration: CacheableItemSource.FetchConfiguration<School>): Flow<List<Cacheable<School>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: CacheableItemSource.FetchConfiguration<School>
    ): Flow<Cacheable<School>> = channelFlow {
        schoolRepository.getById(id.toInt()).collectLatest { cacheableSchool ->
            if (cacheableSchool == null) return@collectLatest send(Cacheable.NotExisting(id))
            send(Cacheable.Loaded(cacheableSchool))
        }
    }
}