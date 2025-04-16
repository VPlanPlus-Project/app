package plus.vplan.app.domain.repository.schulverwalter

interface SchulverwalterRepository {

    /**
     * @return A set of vppIds whose schulverwalter access is not valid anymore. This will fetch the user endpoint
     * at beste.schule to check if the access token is still valid. If the user endpoint is not available due to
     * a network error, the access token will **not** be considered invalid.
     */
    suspend fun checkAccess(): Set<Int>
    suspend fun setSchulverwalterAccessValidity(token: String, valid: Boolean)

    suspend fun setSchulverwalterAccessTokenForUser(vppIdId: Int, token: String)
}