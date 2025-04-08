package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId

interface VppIdRepository: WebEntityRepository<VppId> {
    suspend fun getAccessToken(code: String): Response<String>
    suspend fun getUserByToken(token: String, upsert: Boolean = true): Response<VppId.Active>

    suspend fun getDevices(vppId: VppId.Active): Response<List<VppIdDevice>>
    suspend fun logoutDevice(vppId: VppId.Active, deviceId: Int): Response<Unit>
    suspend fun logout(token: String): Response<Unit>
    suspend fun deleteAccessTokens(vppId: VppId.Active)

    suspend fun getSchulverwalterReauthUrl(vppId: VppId.Active): Response<String>

    fun getVppIds(): Flow<List<VppId>>

    suspend fun sendFeedback(access: SchoolApiAccess, content: String, email: String?): Response<Unit>
    suspend fun updateFirebaseToken(vppId: VppId.Active, token: String): Response.Error?
}

data class VppIdDevice(
    val id: Int,
    val name: String,
    val connectedAt: LocalDateTime,
    val isThisDevice: Boolean
)