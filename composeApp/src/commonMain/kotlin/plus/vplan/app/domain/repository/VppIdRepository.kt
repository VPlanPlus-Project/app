package plus.vplan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId

interface VppIdRepository {
    suspend fun getAccessToken(code: String): Response<String>
    suspend fun getUserByToken(token: String): Response<VppId.Active>

    suspend fun getVppIdById(id: Int): Flow<VppId?>
    suspend fun getVppIdByIdWithCaching(id: Int): Response<VppId>

    fun getVppIds(): Flow<List<VppId>>
}