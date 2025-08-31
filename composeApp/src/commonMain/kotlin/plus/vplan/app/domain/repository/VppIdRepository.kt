package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.base.ItemRepository
import plus.vplan.app.domain.repository.base.ResponsePreference

interface VppIdRepository : ItemRepository<Int, VppId> {
    suspend fun getAccessToken(code: String): Response<String>
    suspend fun getUserByToken(token: String): Response<VppVppIdDto>

    fun getById(id: Int, responsePreference: ResponsePreference): Flow<CacheState<VppId>>

    suspend fun getDevices(vppId: VppId.Active): Response<List<VppIdDevice>>
    suspend fun logoutDevice(vppId: VppId.Active, deviceId: Int): Response<Unit>
    suspend fun logout(token: String): Response<Unit>
    suspend fun deleteAccessTokens(vppId: VppId.Active)

    suspend fun getSchulverwalterReauthUrl(vppId: VppId.Active): Response<String>

    fun getVppIds(): Flow<List<VppId>>

    suspend fun sendFeedback(access: VppSchoolAuthentication, content: String, email: String?): Response<Unit>
    suspend fun updateFirebaseToken(vppId: VppId.Active, token: String): Response.Error?

    suspend fun upsert(entity: VppDbDto)

    suspend fun logSp24Credentials(authentication: VppSchoolAuthentication.Sp24)
}

data class VppIdDevice(
    val id: Int,
    val name: String,
    val connectedAt: LocalDateTime,
    val isThisDevice: Boolean
)

sealed class VppDbDto(
    val id: Int,
    val username: String,
    val groups: List<Int>,
    val creationReason: CreationReason
) {
    class CachedVppDbDto(
        id: Int,
        username: String,
        groups: List<Int>
    ) : VppDbDto(id, username, groups, CreationReason.Cached)

    class AppVppDbDto(
        id: Int,
        username: String,
        groups: List<Int>,
        val schulverwalterUserId: Int?,
        val schulverwalterAccessToken: String?,
        val accessToken: String
    ) : VppDbDto(id, username, groups, CreationReason.Persisted)
}

data class VppVppIdDto(
    val id: Int,
    val username: String,
    val groupId: Int,
    val schoolId: Int,
    val schulverwalterId: Int?,
    val schulverwalterAccessToken: String?
)