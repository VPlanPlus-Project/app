package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMap
import plus.vplan.app.domain.model.data_structure.ConcurrentHashMapFactory
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileSource : KoinComponent {
    private val profileRepository: ProfileRepository by inject()
    private val concurrentHashMapFactory: ConcurrentHashMapFactory by inject()

    private val flows: ConcurrentHashMap<Uuid, StateFlow<CacheState<Profile>>> = concurrentHashMapFactory.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val allProfilesFlow: StateFlow<List<Uuid>> by lazy {
        profileRepository.getAll()
            .map { profiles -> profiles.map { it.id } }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = emptyList()
            )
    }

    fun getAll(): Flow<List<CacheState<Profile>>> {
        return allProfilesFlow.map { profileIds ->
            if (profileIds.isEmpty()) emptyList()
            else profileIds.map { id -> getById(id).value }
        }
    }

    fun getById(id: Uuid): StateFlow<CacheState<Profile>> {
        return flows.getOrPut(id) {
            profileRepository.getById(id)
                .map { profile -> profile?.let { CacheState.Done(it) } ?: CacheState.NotExisting(id.toHexString()) }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = CacheState.Loading(id.toHexString())
                )
        }
    }
}