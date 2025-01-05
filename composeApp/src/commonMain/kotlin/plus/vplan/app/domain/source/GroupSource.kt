package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupRepository

class GroupSource(
    private val groupRepository: GroupRepository
) : CacheableItemSource<Group>() {

    override fun getAll(configuration: FetchConfiguration<Group>): Flow<List<Cacheable<Group>>> {
        TODO("Not yet implemented")
    }

    override fun getById(
        id: String,
        configuration: FetchConfiguration<Group>
    ): Flow<Cacheable<Group>> = channelFlow {
        cache.getOrPut(id) { groupRepository.getById(id.toInt()).distinctUntilChanged() }.collectLatest { cachedGroup ->
            if (cachedGroup == null) return@collectLatest send(Cacheable.NotExisting(id))
            val group = MutableStateFlow(cachedGroup)
            launch { group.collect { send(Cacheable.Loaded(it)) } }
            send(Cacheable.Loaded(cachedGroup))
            if (configuration is Group.Fetch) {
                if (configuration.school is School.Fetch) {
                    launch {
                        App.schoolSource.getById(cachedGroup.school.getItemId(), configuration.school).collect {
                            group.value = group.value.copy(school = it)
                        }
                    }
                }
            }
        }
    }
}