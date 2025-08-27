package plus.vplan.app.domain.repository.base

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Item

interface ItemRepository<ID, I: Item<ID, *>> {
    fun getByLocalId(id: ID): Flow<I?>
    fun getAllLocalIds(): Flow<List<ID>>
}

/**
 * Represents the preference for how to handle responses from a repository.
 */
enum class ResponsePreference {

    /**
     * Will return the fastest available response, which may be cached or outdated. If a refresh is needed,
     * regardless of the cache being expired or explicitly requested, it will be done in the background and
     * the updated version will be delivered later. This is recommended for self-updating UIs where the user
     * should see something immediately, even if it is not the most up-to-date version.
     */
    Fast,

    /**
     * Will return the most up-to-date response, which may take longer to fetch if the cache is expired. If the
     * update fails, it will return the error. This is recommended for situations where the user should see the
     * most recent data, even if it means waiting longer for the response.
     */
    Fresh,

    /**
     * Will try to return the most up-to-date response, but if the cache is expired and the update fails,
     * it will return the fastest available response, which may be cached or outdated.
     * If the cache is expired, it will also try to refresh it in the background. This is recommendet for
     * cases where data should be available but cannot be updated immediately, for example system notifications.
     */
    Secure
}