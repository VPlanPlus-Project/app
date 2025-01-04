package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import plus.vplan.app.App
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.cache.CacheableItemSource
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileSource(
    private val profileRepository: ProfileRepository
) : CacheableItemSource<Profile> {
    override fun getAll(configuration: CacheableItemSource.FetchConfiguration<Profile>): Flow<List<Cacheable<Profile>>> = channelFlow {
        profileRepository.getAll().collectLatest { flowEmission ->
            combine(
                flowEmission.map { getById(it.getItemId(), configuration) }
            ) { it }.collect { send(it.toList()) }
        }
    }

    override fun getById(
        id: String,
        configuration: CacheableItemSource.FetchConfiguration<Profile>
    ): Flow<Cacheable<Profile>> = channelFlow {
        profileRepository.getById(Uuid.parseHex(id)).collectLatest { cacheableProfile ->
            if (cacheableProfile == null) return@collectLatest send(Cacheable.NotExisting(id))
            send(Cacheable.Loaded(cacheableProfile))

            if (configuration is CacheableItemSource.FetchConfiguration.Ignore) return@collectLatest
            if (configuration !is Profile.Fetch) throw IllegalArgumentException("Expected Profile.Fetch Configuration")
            if (cacheableProfile is Profile.StudentProfile && configuration.studentProfile is Profile.StudentProfile.Fetch) {
                if (configuration.studentProfile.group is Group.Fetch) {
                    launch {
                        App.groupSource.getById(cacheableProfile.group.getItemId(), configuration.studentProfile.group).collect { cacheableGroup ->
                            send(Cacheable.Loaded(cacheableProfile.copy(group = cacheableGroup)))
                        }
                    }
                }
            }
        }
    }
}