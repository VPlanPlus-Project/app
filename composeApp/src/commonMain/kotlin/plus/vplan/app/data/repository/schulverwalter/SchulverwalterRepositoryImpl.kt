package plus.vplan.app.data.repository.schulverwalter

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.first
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository

class SchulverwalterRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : SchulverwalterRepository {
    override suspend fun checkAccess(): Set<Int> {
        val invalidVppIds = mutableSetOf<Int>()
        safeRequest(onError = { return emptySet() }) {
            vppDatabase.vppIdDao.getSchulverwalterAccess().first().forEach { access ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        pathSegments = listOf("api", "me")
                    }
                    bearerAuth(access.schulverwalterAccessToken)
                }
                if (response.status == HttpStatusCode.Unauthorized) {
                    Logger.e { "Failed to authenticate ${access.schulverwalterUserId}" }
                    invalidVppIds.add(access.vppId)
                    setSchulverwalterAccessValidity(access.schulverwalterAccessToken, false)
                } else Logger.i { "User ${access.schulverwalterUserId} is still valid" }
            }
        }
        return invalidVppIds
    }

    override suspend fun setSchulverwalterAccessValidity(token: String, valid: Boolean) {
        vppDatabase.vppIdDao.setSchulverwalterValidity(valid, token)
    }

    override suspend fun setSchulverwalterAccessTokenForUser(vppIdId: Int, token: String) {
        val oldAccess = vppDatabase.vppIdDao.getSchulverwalterAccess().first().first { it.vppId == vppIdId }
        vppDatabase.vppIdDao.deleteSchulverwalterAccessToken(vppIdId)
        vppDatabase.vppIdDao.upsert(DbVppIdSchulverwalter(
            vppId = vppIdId,
            schulverwalterUserId = oldAccess.schulverwalterUserId,
            schulverwalterAccessToken = token,
            isValid = true
        ))
    }
}