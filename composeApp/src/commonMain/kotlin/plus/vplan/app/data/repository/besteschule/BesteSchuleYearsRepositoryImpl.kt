package plus.vplan.app.data.repository.besteschule

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleYear
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear
import plus.vplan.app.domain.model.besteschule.api.ApiYear
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleYearsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleYearsRepositoryImpl : BesteSchuleYearsRepository, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()
    private val httpClient by inject<HttpClient>()

    override suspend fun addYearsToCache(years: Set<BesteSchuleYear>) {
        vppDatabase.besteSchuleYearDao.upsert(years.map { year ->
            DbBesteschuleYear(
                id = year.id,
                name = year.name,
                from = year.from,
                to = year.to,
                cachedAt = year.cachedAt
            )
        })
    }

    override fun getYearFromCache(yearId: Int): Flow<BesteSchuleYear?> {
        return vppDatabase.besteSchuleYearDao.getById(yearId).map { it?.toModel() }
    }

    override suspend fun getYearsFromApi(schulverwalterAccessToken: String): Response<List<ApiYear>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "years")
                }
                bearerAuth(schulverwalterAccessToken)
            }

            if (!response.status.isSuccess()) return response.toErrorResponse()

            return Response.Success(response.body<ResponseDataWrapper<List<ApiYear>>>().data)
        }

        return Response.Error.Cancelled
    }

    override fun getYears(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?
    ): Flow<Response<List<BesteSchuleYear>>> = flow {

        // Subscribe to DB changes continuously
        vppDatabase.besteSchuleYearDao.getAll()
            .map { entities -> entities.map { it.toModel() } }
            .collect { cached ->

                val now = Clock.System.now()
                val cacheIsEmpty = cached.isEmpty()
                val cacheIsStale = cached.all { now - it.cachedAt > 1.days }

                when (responsePreference) {

                    ResponsePreference.Fast -> {
                        // Emit cached data immediately
                        emit(Response.Success(cached))

                        // If cache is empty or stale, try background refresh
                        if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null) {
                            try {
                                refreshYearsIfNeeded(contextBesteschuleAccessToken)
                            } catch (_: Exception) {
                                // Ignore errors in fast mode
                            }
                        }
                    }

                    ResponsePreference.Secure -> {
                        if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null) {
                            val refreshed = try {
                                refreshYearsIfNeeded(contextBesteschuleAccessToken)
                            } catch (_: Exception) {
                                null
                            }

                            if (refreshed != null) {
                                emit(refreshed)
                            } else if (cached.isNotEmpty()) {
                                // Fall back to cache if refresh failed
                                emit(Response.Success(cached))
                            } else {
                                emit(Response.Error.Other("Failed to refresh and cache is empty"))
                            }
                        } else {
                            emit(Response.Success(cached))
                        }
                    }

                    ResponsePreference.Fresh -> {
                        // Always refresh from API
                        if (contextBesteschuleAccessToken == null) {
                            throw IllegalStateException("If requesting fresh data, an access token is required.")
                        } else {
                            val refreshed = try {
                                refreshYearsIfNeeded(contextBesteschuleAccessToken)
                            } catch (_: Exception) {
                                null
                            }

                            if (refreshed != null) {
                                emit(refreshed)
                            } else {
                                emit(Response.Error.Other("Failed to refresh data"))
                            }
                        }
                    }
                }
            }
    }

    /**
     * Helper function to refresh years from API and store to DB.
     */
    private suspend fun refreshYearsIfNeeded(token: String): Response<List<BesteSchuleYear>> {
        val apiResponse = getYearsFromApi(token)
        if (apiResponse !is Response.Success) return apiResponse as Response.Error

        val now = Clock.System.now()
        val cachedModels = apiResponse.data.map {
            BesteSchuleYear(
                id = it.id,
                name = it.name,
                from = LocalDate.parse(it.from),
                to = LocalDate.parse(it.to),
                cachedAt = now
            )
        }.toSet()

        // Store refreshed data to DB
        addYearsToCache(cachedModels)
        return Response.Success(cachedModels.toList())
    }
}