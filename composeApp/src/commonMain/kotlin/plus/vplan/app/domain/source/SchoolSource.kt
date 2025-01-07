package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolRepository

class SchoolSource(
    private val schoolRepository: SchoolRepository
) : CacheableItemSource<School>() {

    override fun getAll(configuration: FetchConfiguration<School>): Flow<List<Cacheable<School>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: FetchConfiguration<School>
    ): Flow<Cacheable<School>> {
        return configuredCache.getOrPut("$id--$configuration") {
            channelFlow {
                cache.getOrPut(id) { schoolRepository.getById(id.toInt()).distinctUntilChanged() }.collectLatest { cacheableSchool ->
                    if (cacheableSchool !is Cacheable.Loaded) return@collectLatest send(cacheableSchool)
                    send(Cacheable.Loaded(cacheableSchool.value))
                }
            }
        }
    }
}