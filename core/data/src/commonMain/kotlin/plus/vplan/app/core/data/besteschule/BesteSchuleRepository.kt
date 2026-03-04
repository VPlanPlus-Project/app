package plus.vplan.app.core.data.besteschule

import plus.vplan.app.core.model.VppId

interface BesteSchuleRepository {
    suspend fun checkValidity(schulverwalterUserId: Int): Boolean

    suspend fun saveBesteSchuleAccessValidity(userId: Int, valid: Boolean)
    suspend fun saveBesteSchuleAccess(
        userId: Int,
        vppId: VppId.Active,
        token: String
    )
}