package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleSubject
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleSubjectsRepositoryImpl : BesteSchuleSubjectsRepository, KoinComponent {
    private val besteschuleApiRepository by inject<BesteSchuleApiRepository>()
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleSubject?>>()

    override suspend fun getSubjectsFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentData.Subject>> {
        val response = besteschuleApiRepository.getStudentData(schulverwalterAccessToken)
        if (response is Response.Error) return response

        return Response.Success((response as Response.Success).data.subjects)
    }

    override suspend fun addSubjectsToCache(subjects: Set<BesteSchuleSubject>) {
        vppDatabase.besteSchuleSubjectDao.upsert(subjects.map { subject ->
            DbBesteschuleSubject(
                id = subject.id,
                shortName = subject.shortName,
                longName = subject.fullName,
                cachedAt = subject.cachedAt
            )
        })
    }

    override fun getSubjectFromCache(subjectId: Int): Flow<BesteSchuleSubject?> {
        return cacheFlows.getOrPut(subjectId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleSubjectDao.getById(subjectId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(subjectId)
            }

            shared
        }
    }

    override fun getAllFromCache(): Flow<List<BesteSchuleSubject>> {
        return vppDatabase.besteSchuleSubjectDao.getAll().map { it.map { item -> item.toModel() } }
    }

    private val getSubjectsHotFlows = mutableMapOf<Int, SharedFlow<Response<List<BesteSchuleSubject>>>>()
    override fun getSubjects(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<List<BesteSchuleSubject>>> {
        val key = responsePreference.hashCode() + contextBesteschuleAccessToken.hashCode()
        val constructFlow = { flow {

            // This flow keeps listening to DB updates
            vppDatabase.besteSchuleSubjectDao.getAll()
                .map { embedded -> embedded.map { it.toModel() } }
                .collect { cached ->

            val now = Clock.System.now()
            val cacheIsEmpty = cached.isEmpty()
            val cacheIsStale = cached.all { now - it.cachedAt > 1.days }

            when (responsePreference) {

                ResponsePreference.Fast -> {
                    // Always emit cached data first
                    emit(Response.Success(cached))

                    // In fast mode, stale or missing data triggers a silent background refresh
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null) {
                        try {
                            refreshSubjects(contextBesteschuleAccessToken)
                        } catch (_: Exception) {
                            // Ignore refresh errors in fast mode
                        }
                    }
                }

                ResponsePreference.Secure -> {
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null) {

                        val refreshed = try {
                            refreshSubjects(contextBesteschuleAccessToken)
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
                            emit(Response.Error.Other("Failed to refresh subjects and cache is empty"))
                        }

                    } else {
                        emit(Response.Success(cached))
                    }
                }

                ResponsePreference.Fresh -> {
                    // Fresh mode always requires a successful API update
                    if (contextBesteschuleAccessToken == null) {
                        throw IllegalStateException("When fresh values are requested, a token is required.")
                    } else {
                        val refreshed = try {
                            refreshSubjects(contextBesteschuleAccessToken)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            emit(refreshed)
                        } else {
                            emit(Response.Error.Other("Failed to refresh subjects"))
                        }
                    }
                }
            }
                }
        } }

        if (responsePreference != ResponsePreference.Fresh) return constructFlow()

        return getSubjectsHotFlows.getOrPut(key) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = constructFlow()
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getSubjectsHotFlows.remove(key)
            }

            shared
        }
    }

    private suspend fun refreshSubjects(
        accessToken: String
    ): Response<List<BesteSchuleSubject>> {

        // Load data from API
        val apiResponse = getSubjectsFromApi(accessToken)
        if (apiResponse !is Response.Success) return apiResponse as Response.Error

        val now = Clock.System.now()

        // Convert API model to local model
        val subjects = apiResponse.data.map { api ->
            BesteSchuleSubject(
                id = api.id,
                shortName = api.shortName,
                fullName = api.fullName,
                cachedAt = now
            )
        }.toSet()

        // Insert into DB
        addSubjectsToCache(subjects)

        // Return the new cached model
        return Response.Success(subjects.toList())
    }
}