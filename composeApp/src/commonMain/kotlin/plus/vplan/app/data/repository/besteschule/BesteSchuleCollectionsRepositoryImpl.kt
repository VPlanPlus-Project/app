package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleCollectionsRepositoryImpl : BesteSchuleCollectionsRepository, KoinComponent {
    private val besteschuleApiRepository by inject<BesteSchuleApiRepository>()
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleCollection?>>()

    override suspend fun getCollectionsFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData.Collection>> {
        val response = besteschuleApiRepository.getStudentGradeData(schulverwalterAccessToken)
        if (response !is Response.Success) return response as Response.Error

        return Response.Success(response.data.map { it.collection })
    }

    override suspend fun addCollectionsToCache(items: Set<BesteSchuleCollection>) {
        val existingIntervals = vppDatabase.besteSchuleIntervalDao.getAll().first().map { it.interval.id }
        val existingTeachers = vppDatabase.besteSchuleTeacherDao.getAll().first().map { it.id }
        val existingSubjects = vppDatabase.besteSchuleSubjectDao.getAll().first().map { it.id }

        val items = items
            .filter { it.teacherId in existingTeachers }
            .filter { it.subjectId in existingSubjects }
            .filter { it.intervalId in existingIntervals }

        vppDatabase.besteSchuleCollectionDao.upsert(items.map { collection ->
            DbBesteSchuleCollection(
                id = collection.id,
                type = collection.type,
                name = collection.name,
                subjectId = collection.subjectId,
                givenAt = collection.givenAt,
                intervalId = collection.intervalId,
                teacherId = collection.teacherId,
                cachedAt = collection.cachedAt
            )
        })
    }

    override fun getFromCache(collectionId: Int): Flow<BesteSchuleCollection?> {
        return cacheFlows.getOrPut(collectionId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleCollectionDao.getById(collectionId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(collectionId)
            }

            shared
        }
    }

    override fun getAllFromCache(): Flow<List<BesteSchuleCollection>> {
        return vppDatabase.besteSchuleCollectionDao.getAll().map { it.map { item -> item.toModel() } }
    }

    private val getCollectionsHotFlows = mutableMapOf<Int, SharedFlow<Response<List<BesteSchuleCollection>>>>()
    override fun getCollections(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
    ): Flow<Response<List<BesteSchuleCollection>>> {
        val key = responsePreference.hashCode() + contextBesteschuleAccessToken.hashCode()
        val constructFlow = { flow {

            // This flow keeps listening to DB updates
            vppDatabase.besteSchuleCollectionDao.getAll()
                .map { items -> items.map { it.toModel() } }
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
                            refreshCollections(contextBesteschuleAccessToken)
                        } catch (_: Exception) {
                            // Ignore refresh errors in fast mode
                        }
                    }
                }

                ResponsePreference.Secure -> {
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null) {

                        val refreshed = try {
                            refreshCollections(contextBesteschuleAccessToken)
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
                            emit(Response.Error.Other("Failed to refresh collections and cache is empty"))
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
                            refreshCollections(contextBesteschuleAccessToken)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            emit(refreshed)
                        } else {
                            emit(Response.Error.Other("Failed to refresh collections"))
                        }
                    }
                }
            }
                }
        } }

        if (responsePreference != ResponsePreference.Fresh) return constructFlow()

        return getCollectionsHotFlows.getOrPut(key) {
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
                getCollectionsHotFlows.remove(key)
            }

            shared
        }
    }

    private suspend fun refreshCollections(
        accessToken: String,
    ): Response<List<BesteSchuleCollection>> {

        // Load data from API
        val apiResponse = getCollectionsFromApi(accessToken)
        if (apiResponse !is Response.Success) return apiResponse as Response.Error

        val now = Clock.System.now()

        // Convert API model to local model
        val collection = apiResponse.data.map { api ->
            BesteSchuleCollection(
                id = api.id,
                name = api.name,
                type = api.type,
                subjectId = api.subjectId,
                givenAt = LocalDate.parse(api.givenAt),
                intervalId = api.intervalId,
                teacherId = api.teacherId,
                cachedAt = now
            )
        }.toSet()

        // Insert into DB (existing years are required here)
        addCollectionsToCache(collection)

        // Return the new cached model
        return Response.Success(collection.toList())
    }
}