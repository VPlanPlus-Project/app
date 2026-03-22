package plus.vplan.app.core.data.vpp_id

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.core.model.NetworkException
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication

interface VppIdRepository {
    /**
     * Exchanges an OAuth authorization code for an access token.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun getAccessToken(code: String): String

    /**
     * Fetches user info for the given access token from the server.
     * Throws [NetworkException] on network/server errors (including [NetworkErrorKind.Unauthorized]).
     */
    suspend fun getUserByToken(token: String): VppUserInfo

    /**
     * Returns a Flow that emits the VppId with the given [id] from the local database,
     * or `null` if it doesn't exist locally. Does not trigger any network download.
     */
    fun getById(id: Int, forceReload: Boolean = false): Flow<VppId?>

    /** Returns a Flow of all locally-cached VppIds. */
    fun getVppIds(): Flow<List<VppId>>

    /** Returns a Flow of all local integer IDs for VppIds. */
    fun getAllLocalIds(): Flow<List<Int>>

    /**
     * Fetches the list of devices connected to this VppId account.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun getDevices(vppId: VppId.Active): List<VppIdDevice>

    /**
     * Logs out the given device from this VppId account.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun logoutDevice(vppId: VppId.Active, deviceId: Int)

    /**
     * Logs out using the given token from the OAuth server.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun logout(token: String)

    /** Deletes stored access tokens for the given VppId from the local database. */
    suspend fun deleteAccessTokens(vppId: VppId.Active)

    /**
     * Returns the URL to re-authenticate with Schulverwalter for the given VppId.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun getSchulverwalterReauthUrl(vppId: VppId.Active): String

    /**
     * Sends feedback with the given [access] credentials.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun sendFeedback(access: VppSchoolAuthentication, content: String, email: String?)

    /**
     * Updates the Firebase token for push notifications for the given VppId.
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun updateFirebaseToken(vppId: VppId.Active, token: String)

    /**
     * Saves (upserts) the given [VppId] to the local database.
     * Handles both [VppId.Cached] and [VppId.Active].
     */
    suspend fun save(vppId: VppId)

    /**
     * Downloads the latest data for the given VppId id and saves it locally.
     * Returns `null` if the VppId does not exist on the server (and deletes local copy).
     * Throws [NetworkException] on network/server errors.
     */
    suspend fun downloadAndSave(id: Int): VppId?

    /**
     * Logs SP24 credentials to the backend (fire-and-forget, never throws).
     */
    suspend fun logSp24Credentials(authentication: VppSchoolAuthentication.Sp24)

    /**
     * Checks whether the given access token is still valid against the server.
     * Returns `true` if the token is valid (200 OK), `false` if the token is rejected (401).
     * Throws [NetworkException] for all other network/server errors.
     */
    suspend fun checkTokenValidity(accessToken: String): Boolean
}

data class VppIdDevice(
    val id: Int,
    val name: String,
    val connectedAt: LocalDateTime,
    val isThisDevice: Boolean
)

data class VppUserInfo(
    val id: Int,
    val username: String,
    val groupId: Int,
    val schoolId: Int,
    val schulverwalterId: Int?,
    val schulverwalterAccessToken: String?
)
