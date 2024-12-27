package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId

interface VppIdRepository {
    suspend fun getAccessToken(code: String): Response<String>
    suspend fun getUserByToken(token: String, upsert: Boolean = true): Response<VppId.Active>

    suspend fun getVppIdById(id: Int): Flow<VppId?>
    suspend fun getVppIdByIdWithCaching(id: Int): Response<VppId>

    suspend fun getDevices(vppId: VppId.Active): Response<List<VppIdDevice>>
    suspend fun logoutDevice(vppId: VppId.Active, deviceId: Int): Response<Unit>
    suspend fun logout(token: String): Response<Unit>
    suspend fun deleteAccessTokens(vppId: VppId.Active)

    fun getVppIds(): Flow<List<VppId>>
}

data class VppIdDevice(
    val id: Int,
    val name: String,
    val connectedAt: LocalDateTime
)