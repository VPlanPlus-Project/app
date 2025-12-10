package plus.vplan.app.data.repository.schulverwalter

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.repository.schulverwalter.api.Me
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbVppIdSchulverwalter
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterUserData
import kotlin.time.Clock

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

    override suspend fun getUserData(schulverwalterAccessToken: String): Response<SchulverwalterUserData> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "me")
                }
                bearerAuth(schulverwalterAccessToken)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()

            val data = response.body<ResponseDataWrapper<Me>>().data

            return Response.Success(
                data = SchulverwalterUserData(
                    year = SchulverwalterUserData.Year(
                        intervals = data.students.first().users.first().year.intervals.map { interval ->
                            Interval(
                                id = interval.id,
                                name = interval.name,
                                type = Interval.Type.fromString(interval.type),
                                from = LocalDate.parse(interval.from),
                                to = LocalDate.parse(interval.to),
                                includedIntervalId = interval.includedIntervalId,
                                yearId = interval.yearId,
                                cachedAt = Clock.System.now(),
                                collectionIds = emptyList(),
                                linkedWithSchulverwalterUserIds = emptySet(),
                            )
                        }
                    )
                )
            )
        }

        return Response.Error.Cancelled
    }
}