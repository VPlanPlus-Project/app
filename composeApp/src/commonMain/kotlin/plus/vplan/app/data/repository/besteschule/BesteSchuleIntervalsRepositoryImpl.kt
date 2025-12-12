package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleIntervalUser
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleIntervalsRepositoryImpl : BesteSchuleIntervalsRepository, KoinComponent {
    private val besteschuleApiRepository by inject<BesteSchuleApiRepository>()
    private val vppDatabase by inject<VppDatabase>()

    override suspend fun getIntervalsFromApi(schulverwalterAccessToken: String, withCache: Boolean): Response<List<ApiStudentData.Interval>> {
        val response = besteschuleApiRepository.getStudentData(schulverwalterAccessToken, withCache)
        if (response is Response.Error) return response

        return Response.Success((response as Response.Success).data.intervals)
    }

    override suspend fun addIntervalsToCache(intervals: Set<BesteSchuleInterval>) {
        val existingYears = vppDatabase.besteSchuleYearDao.getAll().first().map { it.id }.toSet()
        val existingIntervals = vppDatabase.besteSchuleIntervalDao.getAll().first().map { it.interval.id }
            .toSet() + intervals.map { it.id }

        val intervals = intervals
            .filter { it.yearId in existingYears }
            .filter { it.includedIntervalId == null || it.includedIntervalId in existingIntervals }

        vppDatabase.besteSchuleIntervalDao.upsert(intervals.map { interval ->
            DbBesteSchuleInterval(
                id = interval.id,
                name = interval.name,
                type = interval.type.name,
                from = interval.from,
                to = interval.to,
                includedIntervalId = interval.includedIntervalId,
                yearId = interval.yearId,
                cachedAt = interval.cachedAt
            )
        })

        vppDatabase.besteSchuleIntervalDao.upsertUserMappings(intervals.flatMap { interval ->
            interval.linkedToSchulverwalterAccountIds.map { accountId ->
                DbBesteschuleIntervalUser(
                    intervalId = interval.id,
                    schulverwalterUserId = accountId
                )
            }
        })
    }

    override fun getIntervalFromCache(intervalId: Int): Flow<BesteSchuleInterval?> {
        return vppDatabase.besteSchuleIntervalDao.getById(intervalId).map { it?.toModel() }
    }

    override fun getIntervals(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?,
        withCache: Boolean
    ): Flow<Response<List<BesteSchuleInterval>>> = flow {

        // This flow keeps listening to DB updates
        val dbFlow = if (contextBesteschuleUserId != null) {
            vppDatabase.besteSchuleIntervalDao.getIntervalsForUser(contextBesteschuleUserId)
        } else {
            vppDatabase.besteSchuleIntervalDao.getAll()
        }.map { embedded ->
            embedded.map { it.toModel() }
        }

        dbFlow.collect { cached ->

            val now = Clock.System.now()
            val cacheIsEmpty = cached.isEmpty()
            val cacheIsStale = cached.all { now - it.cachedAt > 1.days }

            when (responsePreference) {

                ResponsePreference.Fast -> {
                    // Always emit cached data first
                    emit(Response.Success(cached))

                    // In fast mode, stale or missing data triggers a silent background refresh
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null && contextBesteschuleUserId != null) {
                        try {
                            refreshIntervals(contextBesteschuleAccessToken, contextBesteschuleUserId, withCache)
                        } catch (_: Exception) {
                            // Ignore refresh errors in fast mode
                        }
                    }
                }

                ResponsePreference.Secure -> {
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null && contextBesteschuleUserId != null) {

                        val refreshed = try {
                            refreshIntervals(contextBesteschuleAccessToken, contextBesteschuleUserId, withCache)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            // A successful refresh produces updated DB rows, which will re-trigger DB flow
                            emit(refreshed)
                        } else if (cached.isNotEmpty()) {
                            // Fallback to existing cache
                            emit(Response.Success(cached))
                        } else {
                            emit(Response.Error.Other("Failed to refresh intervals and cache is empty"))
                        }

                    } else {
                        emit(Response.Success(cached))
                    }
                }

                ResponsePreference.Fresh -> {
                    // Fresh mode always requires a successful API update
                    if (contextBesteschuleAccessToken == null) {
                        throw IllegalStateException("When fresh values are requested, a token is required.")
                    } else if (contextBesteschuleUserId == null) {
                        throw IllegalStateException("When fresh values are requested, the user id is required.")
                    } else {
                        val refreshed = try {
                            refreshIntervals(contextBesteschuleAccessToken, contextBesteschuleUserId, withCache)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            emit(refreshed)
                        } else {
                            emit(Response.Error.Other("Failed to refresh intervals"))
                        }
                    }
                }
            }
        }
    }

    private suspend fun refreshIntervals(
        accessToken: String,
        userId: Int,
        withCache: Boolean
    ): Response<List<BesteSchuleInterval>> {

        // Load data from API
        val apiResponse = getIntervalsFromApi(accessToken, withCache)
        if (apiResponse !is Response.Success) return apiResponse as Response.Error

        val now = Clock.System.now()

        // Convert API model to local model
        val intervals = apiResponse.data.map { api ->
            BesteSchuleInterval(
                id = api.id,
                name = api.name,
                type = BesteSchuleInterval.Type.fromString(api.type),
                from = LocalDate.parse(api.from),
                to = LocalDate.parse(api.to),
                includedIntervalId = api.includedIntervalId,
                yearId = api.yearId,
                linkedToSchulverwalterAccountIds = setOf(userId),
                cachedAt = now
            )
        }.toSet()

        // Insert into DB (existing years are required here)
        addIntervalsToCache(intervals)

        // Return the new cached model
        return Response.Success(intervals.toList())
    }

}