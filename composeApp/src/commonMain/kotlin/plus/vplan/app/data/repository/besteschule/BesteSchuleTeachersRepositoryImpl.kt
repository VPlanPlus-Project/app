package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleTeacher
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.PrefetchError
import plus.vplan.app.domain.repository.base.PrefetchHandler
import plus.vplan.app.domain.repository.base.PrefetchInstruction
import plus.vplan.app.domain.repository.base.PrefetchRegistry
import plus.vplan.app.domain.repository.base.PrefetchResult
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.base.nestedInstructions
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleTeachersRepositoryImpl : BesteSchuleTeachersRepository, KoinComponent {
    private val besteschuleApiRepository by inject<BesteSchuleApiRepository>()
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleTeacher?>>()

    // Cache flows for prefetching - stores MutableStateFlow for direct value updates
    private val prefetchCacheFlows = mutableMapOf<Int, MutableStateFlow<BesteSchuleTeacher?>>()

    private fun getOrCreatePrefetchFlow(id: Int, initialValue: BesteSchuleTeacher? = null): MutableStateFlow<BesteSchuleTeacher?> {
        return prefetchCacheFlows.getOrPut(id) {
            MutableStateFlow(initialValue)
        }
    }

    override val entityName: String = "teacher"

    init {
        registerForPrefetching()
    }

    override fun registerForPrefetching() {
        PrefetchRegistry.register(entityName, object : PrefetchHandler {
            override suspend fun prefetch(ids: List<Int>, includes: Map<String, PrefetchInstruction>): PrefetchResult {
                return this@BesteSchuleTeachersRepositoryImpl.prefetchByIds(ids, includes)
            }
        })
    }

    override fun unregisterFromPrefetching() {
        PrefetchRegistry.unregister(entityName)
    }

    override suspend fun getTeachersFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData.Teacher>> {
        val response = besteschuleApiRepository.getStudentGradeData(schulverwalterAccessToken)
        if (response !is Response.Success) return response as Response.Error

        return Response.Success(response.data.map { it.teacher }.distinctBy { it.id })
    }

    override suspend fun addTeachersToCache(items: List<BesteSchuleTeacher>) = withContext(Dispatchers.IO) {
        vppDatabase.besteSchuleTeacherDao.upsert(items.map { teacher ->
            DbBesteschuleTeacher(
                id = teacher.id,
                localId = teacher.localId,
                forename = teacher.forename,
                surname = teacher.surname,
                cachedAt = teacher.cachedAt
            )
        })
    }

    override fun getTeacherFromCache(teacherId: Int): Flow<BesteSchuleTeacher?> {
        return cacheFlows.getOrPut(teacherId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleTeacherDao.getTeacher(teacherId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000, // when last subscriber leaves, wait 5s then stop
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1 // last value available immediately for new subscribers
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(teacherId)
            }

            shared
        }
    }

    private val getTeachersHotFlows = mutableMapOf<Int, SharedFlow<Response<List<BesteSchuleTeacher>>>>()
    override fun getTeachers(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        includes: Map<String, PrefetchInstruction>
    ): Flow<Response<List<BesteSchuleTeacher>>> {
        val key = responsePreference.hashCode() + contextBesteschuleAccessToken.hashCode()
        val constructFlow = { flow {

            // This flow keeps listening to DB updates
            vppDatabase.besteSchuleTeacherDao.getAll()
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
                                    refreshTeachers(contextBesteschuleAccessToken)
                                } catch (_: Exception) {
                                    // Ignore refresh errors in fast mode
                                }
                            }
                        }

                        ResponsePreference.Secure -> {
                            if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null) {

                                val refreshed = try {
                                    refreshTeachers(contextBesteschuleAccessToken)
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
                                    emit(Response.Error.Other("Failed to refresh teachers and cache is empty"))
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
                                    refreshTeachers(contextBesteschuleAccessToken)
                                } catch (_: Exception) {
                                    null
                                }

                                if (refreshed != null) {
                                    emit(refreshed)
                                } else {
                                    emit(Response.Error.Other("Failed to refresh teachers"))
                                }
                            }
                        }
                    }
                }
        } }

        if (responsePreference != ResponsePreference.Fresh) return constructFlow()

        return getTeachersHotFlows.getOrPut(key) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = constructFlow()
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000, // when last subscriber leaves, wait 5s then stop
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1 // last value available immediately for new subscribers
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getTeachersHotFlows.remove(key)
            }

            shared
        }
    }

    private suspend fun refreshTeachers(
        accessToken: String
    ): Response<List<BesteSchuleTeacher>> = withContext(Dispatchers.IO) {

        // Load data from API
        val apiResponse = getTeachersFromApi(accessToken)
        if (apiResponse !is Response.Success) return@withContext apiResponse as Response.Error

        val now = Clock.System.now()

        // Convert API model to local model
        val teachers = apiResponse.data.map { api ->
            BesteSchuleTeacher(
                id = api.id,
                localId = api.shortName,
                forename = api.forename,
                surname = api.lastname,
                cachedAt = now
            )
        }

        // Insert into DB
        addTeachersToCache(teachers)

        // Return the new cached model
        return@withContext Response.Success(teachers)
    }

    override suspend fun prefetchByIds(ids: List<Int>, includes: Map<String, PrefetchInstruction>): PrefetchResult {
        if (ids.isEmpty()) return PrefetchResult(0, emptyList())

        val errors = mutableListOf<PrefetchError>()
        var successCount = 0

        try {
            // Batch fetch from DB
            val teachers = vppDatabase.besteSchuleTeacherDao.getByIds(ids)

            // Populate cache flows
            teachers.forEach { dbTeacher ->
                try {
                    val model = dbTeacher.toModel()
                    getOrCreatePrefetchFlow(dbTeacher.id, model).value = model
                    successCount++
                } catch (e: Exception) {
                    errors.add(PrefetchError(entityName, dbTeacher.id, e))
                }
            }

            // Track missing IDs as errors
            val foundIds = teachers.map { it.id }.toSet()
            ids.filter { it !in foundIds }.forEach { missingId ->
                errors.add(PrefetchError(entityName, missingId, Exception("Teacher not found in database")))
            }

        } catch (e: Exception) {
            ids.forEach { id ->
                errors.add(PrefetchError(entityName, id, e))
            }
        }

        return PrefetchResult(successCount, errors)
    }
}
