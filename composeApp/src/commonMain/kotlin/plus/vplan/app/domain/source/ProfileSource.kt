package plus.vplan.app.domain.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileSource(
    private val profileRepository: ProfileRepository
) {
    private val cache = hashMapOf<Uuid, Flow<CacheState<Profile>>>()
    private val cacheItems = hashMapOf<Uuid, CacheState<Profile>>()
    private var allProfilesFlow: Flow<List<CacheState<Profile>>>? = null

    fun getAll(): Flow<List<CacheState<Profile>>> {
        return allProfilesFlow ?: run {
            return@run channelFlow<List<CacheState<Profile>>> {
                profileRepository.getAll().map { it.map { it.id } }
                    .collectLatest {
                        combine(it.map { getById(it) }) { it.toList() }.collectLatest { send(it) }
                    }
            }.also { allProfilesFlow = it }
        }
    }

    fun getById(id: Uuid): Flow<CacheState<Profile>> {
        return cache.getOrPut(id) { profileRepository.getById(id).map { profile -> profile?.let { CacheState.Done(it).also { cacheItems[id] = it } } ?: CacheState.NotExisting(id.toHexString()) } }
    }

    suspend fun getSingleById(id: Uuid): Profile? {
        return (cacheItems[id] as? CacheState.Done<Profile>)?.data ?: getById(id).getFirstValue()
    }
}