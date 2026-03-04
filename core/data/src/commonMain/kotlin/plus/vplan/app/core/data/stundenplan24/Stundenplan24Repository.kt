package plus.vplan.app.core.data.stundenplan24

import plus.vplan.app.core.model.NetworkException
import plus.vplan.lib.sp24.source.Authentication
import plus.vplan.lib.sp24.source.Stundenplan24Client

interface Stundenplan24Repository {
    /**
     * Checks whether the given credentials are valid.
     * Returns `true` if credentials are valid, `false` if credentials are rejected (wrong password).
     * Throws [NetworkException] on any network or server error.
     */
    suspend fun checkCredentials(authentication: Authentication): Boolean

    /**
     * Returns a [Stundenplan24Client] for the given authentication.
     * If [withCache] is `true`, the client has internal caching enabled (useful for bulk fetching).
     */
    suspend fun getSp24Client(authentication: Authentication, withCache: Boolean): Stundenplan24Client
}
