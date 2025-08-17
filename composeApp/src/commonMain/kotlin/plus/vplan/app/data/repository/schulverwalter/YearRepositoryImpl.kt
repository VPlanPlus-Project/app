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
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterYear
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Year
import plus.vplan.app.domain.repository.schulverwalter.YearRepository
import plus.vplan.app.utils.sendAll

class YearRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : YearRepository {
    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Year>> = channelFlow {
        val yearFlow = vppDatabase.yearDao.getById(id).map { it?.toModel() }
        if (!forceReload) {
            var hadData = false
            sendAll(yearFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
            if (hadData) return@channelFlow
        }
        send(CacheState.Loading(id.toString()))

        safeRequest(onError = { trySend(CacheState.Error(id, it)) }) {
            val existing = vppDatabase.yearDao.getById(id).first()
            val accessTokens = existing?.let { listOfNotNull(vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(it.year.userForRequest).first()) }
                ?: vppDatabase.vppIdDao.getSchulverwalterAccess().first()

            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "years")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse()))
                val data = ResponseDataWrapper.fromJson<List<YearItemResponse>>(response.bodyAsText())
                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                vppDatabase.yearDao.upsert(
                    years = data.map {
                        DbSchulverwalterYear(
                            id = it.id,
                            name = it.name,
                            from = LocalDate.parse(it.from),
                            to = LocalDate.parse(it.to),
                            userForRequest = accessToken.schulverwalterUserId,
                            cachedAt = Clock.System.now()
                        )
                    },
                    intervalsCrossovers = data.flatMap { year ->
                        year.intervals.map { interval ->
                            FKSchulverwalterYearSchulverwalterInterval(
                                yearId = year.id,
                                intervalId = interval.id
                            )
                        }
                    },
                    intervals = data.flatMap { year ->
                        year.intervals.map { interval ->
                            DbSchulverwalterInterval(
                                id = interval.id,
                                name = interval.name,
                                type = interval.type,
                                from = LocalDate.parse(interval.from),
                                to = LocalDate.parse(interval.to),
                                includedIntervalId = interval.includedIntervalId,
                                userForRequest = accessToken.schulverwalterUserId,
                                cachedAt = Clock.System.now()
                            )
                        }
                    }
                )
                data.forEach { year ->
                    vppDatabase.yearDao.deleteSchulverwalterYearSchulverwalterInterval(year.id, year.intervals.map { it.id })
                }
            }

            if (yearFlow.first() == null) return@channelFlow send(CacheState.NotExisting(id.toString()))
            return@channelFlow sendAll(getById(id, false))
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.yearDao.getAll()

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
                        pathSegments = listOf("api", "years")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@forEach
                val data = ResponseDataWrapper.fromJson<List<YearItemResponse>>(response.bodyAsText())
                    ?: return@forEach
                vppDatabase.yearDao.upsert(
                    years = data.map {
                        DbSchulverwalterYear(
                            id = it.id,
                            name = it.name,
                            from = LocalDate.parse(it.from),
                            to = LocalDate.parse(it.to),
                            userForRequest = accessToken.schulverwalterUserId,
                            cachedAt = Clock.System.now()
                        )
                    },
                    intervalsCrossovers = data.flatMap { year ->
                        year.intervals.map { interval ->
                            FKSchulverwalterYearSchulverwalterInterval(
                                yearId = year.id,
                                intervalId = interval.id
                            )
                        }
                    },
                    intervals = data.flatMap { year ->
                        year.intervals.map { interval ->
                            DbSchulverwalterInterval(
                                id = interval.id,
                                name = interval.name,
                                type = interval.type,
                                from = LocalDate.parse(interval.from),
                                to = LocalDate.parse(interval.to),
                                includedIntervalId = interval.includedIntervalId,
                                userForRequest = accessToken.schulverwalterUserId,
                                cachedAt = Clock.System.now()
                            )
                        }
                    }
                )
                data.forEach { year ->
                    vppDatabase.yearDao.deleteSchulverwalterYearSchulverwalterInterval(year.id, year.intervals.map { it.id })
                }
                ids.addAll(data.map { it.id })
            }
            return Response.Success(ids)
        }
        return Response.Error.Cancelled
    }
}

@Serializable
private data class YearItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("intervals") val intervals: List<Interval>
) {
    @Serializable
    data class Interval(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String,
        @SerialName("type") val type: String,
        @SerialName("from") val from: String,
        @SerialName("to") val to: String,
        @SerialName("included_interval_id") val includedIntervalId: Int?,
    )
}