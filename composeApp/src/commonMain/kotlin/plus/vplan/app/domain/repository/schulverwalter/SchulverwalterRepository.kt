package plus.vplan.app.domain.repository.schulverwalter

interface SchulverwalterRepository {

    /**
     * @return A set of vppIds whose schulverwalter access is not valid
     */
    suspend fun checkAccess(): Set<Int>
    suspend fun setSchulverwalterAccessValidity(token: String, valid: Boolean)

    suspend fun setSchulverwalterAccessTokenForUser(vppIdId: Int, token: String)
}