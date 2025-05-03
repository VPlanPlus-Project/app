package plus.vplan.app.data.repository.schulverwalter

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.repository.schulverwalter.IntervalRepository
import plus.vplan.app.utils.sendAll

class IntervalRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
): IntervalRepository {
    override suspend fun download(): Response<Set<Int>> {
        safeRequest(onError = { return it }) {
            val accessTokens = vppDatabase.vppIdDao.getSchulverwalterAccess().first().filter { it.isValid != false }
            val ids = mutableSetOf<Int>()
            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "intervals")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@forEach
                val data = ResponseDataWrapper.fromJson<List<IntervalItemResponse>>(response.bodyAsText())
                    ?: return@forEach
                vppDatabase.intervalDao.upsert(
                    intervals = data.map {
                        DbSchulverwalterInterval(
                            id = it.id,
                            name = it.name,
                            type = it.type,
                            from = LocalDate.parse(it.from),
                            to = LocalDate.parse(it.to),
                            includedIntervalId = it.includedIntervalId,
                            userForRequest = accessToken.schulverwalterUserId,
                            cachedAt = Clock.System.now()
                        )
                    },
                    intervalYearCrossovers = data.map { interval ->
                        FKSchulverwalterYearSchulverwalterInterval(
                            yearId = interval.year,
                            intervalId = interval.id
                        )
                    }
                )
                data.forEach { interval ->
                    vppDatabase.intervalDao.deleteSchulverwalterYearSchulverwalterInterval(intervalId = interval.id, yearIds = listOf(interval.year))
                }
                ids.addAll(data.map { it.id })
            }
            return Response.Success(ids)
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Interval>> = channelFlow {
        val intervalFlow = vppDatabase.intervalDao.getById(id).map { it?.toModel() }
        if (!forceReload) {
            var hadData = false
            sendAll(intervalFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
            if (hadData) return@channelFlow
        }
        send(CacheState.Loading(id.toString()))

        safeRequest(onError = { trySend(CacheState.Error(id, it)) }) {
            val existing = vppDatabase.intervalDao.getById(id).first()
            val accessTokens = existing?.let { listOfNotNull(vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(it.interval.userForRequest).first()) }
                ?: vppDatabase.vppIdDao.getSchulverwalterAccess().first()

            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "intervals")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse<Interval>()))
                val data = ResponseDataWrapper.fromJson<List<IntervalItemResponse>>(response.bodyAsText())
                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                vppDatabase.intervalDao.upsert(
                    intervals = data.map {
                        DbSchulverwalterInterval(
                            id = it.id,
                            name = it.name,
                            type = it.type,
                            from = LocalDate.parse(it.from),
                            to = LocalDate.parse(it.to),
                            includedIntervalId = it.includedIntervalId,
                            userForRequest = accessToken.schulverwalterUserId,
                            cachedAt = Clock.System.now()
                        )
                    },
                    intervalYearCrossovers = data.map { interval ->
                        FKSchulverwalterYearSchulverwalterInterval(
                            yearId = interval.year,
                            intervalId = interval.id
                        )
                    }
                )
                data.forEach { interval ->
                    vppDatabase.intervalDao.deleteSchulverwalterYearSchulverwalterInterval(intervalId = interval.id, yearIds = listOf(interval.year))
                }
            }

            if (intervalFlow.first() == null) return@channelFlow send(CacheState.NotExisting(id.toString()))
            return@channelFlow sendAll(getById(id, false))
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.intervalDao.getAll()
}

@Serializable
private data class IntervalItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("included_interval_id") val includedIntervalId: Int?,
    @SerialName("year_id") val year: Int,
)
