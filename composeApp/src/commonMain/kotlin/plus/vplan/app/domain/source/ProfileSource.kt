package plus.vplan.app.domain.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.cache.getFirstValueOld
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.ProfileRepository
import kotlin.uuid.Uuid

class ProfileSource(
    private val profileRepository: ProfileRepository
) {
    private val flows = hashMapOf<Uuid, MutableSharedFlow<CacheStateOld<Profile>>>()
    private val cacheItems = hashMapOf<Uuid, CacheStateOld<Profile>>()
    private var allProfilesFlow: Flow<List<CacheStateOld<Profile>>>? = null

    fun getAll(): Flow<List<CacheStateOld<Profile>>> {
        return allProfilesFlow ?: run {
            return@run channelFlow<List<CacheStateOld<Profile>>> {
                profileRepository.getAll().map { it.map { it.id } }
                    .collectLatest {
                        if (it.isEmpty()) send(emptyList())
                        else combine(it.map { getById(it) }) { it.toList() }.collectLatest { send(it) }
                    }
            }.also { allProfilesFlow = it }
        }
    }

    fun getById(id: Uuid): Flow<CacheStateOld<Profile>> {
        return flows.getOrPut(id) {
            val flow = MutableSharedFlow<CacheStateOld<Profile>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            CoroutineScope(Dispatchers.IO).launch {
                profileRepository.getById(id).map { profile -> profile?.let { CacheStateOld.Done(it).also { cacheItems[id] = it } } ?: CacheStateOld.NotExisting(id.toHexString()) }
                    .collectLatest { flow.tryEmit(it) }
            }
            flow
        }
    }

    suspend fun getSingleById(id: Uuid): Profile? {
        return (cacheItems[id] as? CacheStateOld.Done<Profile>)?.data ?: getById(id).getFirstValueOld()
    }
}