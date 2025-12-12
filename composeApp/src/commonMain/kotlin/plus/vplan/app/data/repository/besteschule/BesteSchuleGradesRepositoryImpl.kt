package plus.vplan.app.data.repository.besteschule

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
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
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleGrade
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.api.ApiStudentGradesData
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleApiRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class BesteSchuleGradesRepositoryImpl : BesteSchuleGradesRepository, KoinComponent {
    private val besteschuleApiRepository by inject<BesteSchuleApiRepository>()
    private val vppDatabase by inject<VppDatabase>()
    private val httpClient by inject<HttpClient>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleGrade?>>()

    override suspend fun getGradesFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentGradesData>> {
        val response = besteschuleApiRepository.getStudentGradeData(schulverwalterAccessToken)
        if (response !is Response.Success) return response as Response.Error

        return Response.Success(response.data)
    }

    override suspend fun getGradeFromApi(
        gradeId: Int,
        schulverwalterAccessToken: String
    ): Response<ApiStudentGradesData> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "grades", gradeId.toString())
                    parameters.append("include", "collection")
                }
                bearerAuth(schulverwalterAccessToken)
            }

            if (!response.status.isSuccess()) return response.toErrorResponse()

            return Response.Success(response.body<ResponseDataWrapper<ApiStudentGradesData>>().data)
        }

        return Response.Error.Cancelled
    }

    override suspend fun addGradesToCache(items: List<BesteSchuleGrade>) {
        val existingCollectionIds = vppDatabase.besteSchuleCollectionDao.getAll().first().map { it.id }.toSet()

        val items = items
            .filter { it.collectionId in existingCollectionIds }
        vppDatabase.besteSchuleGradesDao.upsert(items.map { grade ->
            DbBesteSchuleGrade(
                id = grade.id,
                value = grade.value,
                isOptional = grade.isOptional,
                isSelectedForFinalGrade = grade.isSelectedForFinalGrade,
                schulverwalterUserId = grade.schulverwalterUserId,
                collectionId = grade.collectionId,
                givenAt = grade.givenAt,
                cachedAt = grade.cachedAt
            )
        })
    }

    override fun getGradeFromCache(gradeId: Int): Flow<BesteSchuleGrade?> {
        return cacheFlows.getOrPut(gradeId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleGradesDao.getById(gradeId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(gradeId)
            }

            shared
        }
    }

    private val getGradesHotFlows = mutableMapOf<Int, SharedFlow<Response<List<BesteSchuleGrade>>>>()
    override fun getGrades(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<List<BesteSchuleGrade>>> {
        val key = responsePreference.hashCode() + contextBesteschuleAccessToken.hashCode() + (contextBesteschuleUserId ?: 0)
        val constructFlow = { flow {
            val dbFlow = if (contextBesteschuleUserId != null) {
                vppDatabase.besteSchuleGradesDao.getAllForUser(contextBesteschuleUserId)
            } else {
                vppDatabase.besteSchuleGradesDao.getAll()
            }.map { db -> db.map { it.toModel() } }

            dbFlow.collect { cached ->
            val now = Clock.System.now()
            val cacheIsEmpty = cached.isEmpty()
            val cacheIsStale = cached.all { now - it.cachedAt > 1.days }

            when (responsePreference) {
                ResponsePreference.Fast -> {
                    emit(Response.Success(cached))

                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null && contextBesteschuleUserId != null) {
                        try {
                            refreshGrades(contextBesteschuleAccessToken, contextBesteschuleUserId)
                        } catch (_: Exception) {
                        }
                    }
                }

                ResponsePreference.Secure -> {
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null && contextBesteschuleUserId != null) {
                        val refreshed = try {
                            refreshGrades(contextBesteschuleAccessToken, contextBesteschuleUserId)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            emit(refreshed)
                        } else if (cached.isNotEmpty()) {
                            emit(Response.Success(cached))
                        } else {
                            emit(Response.Error.Other("Failed to refresh grades and cache is empty"))
                        }
                    } else {
                        emit(Response.Success(cached))
                    }
                }

                ResponsePreference.Fresh -> {
                    if (contextBesteschuleAccessToken == null) {
                        throw IllegalStateException("When fresh values are requested, a token is required.")
                    } else if (contextBesteschuleUserId == null) {
                        throw IllegalStateException("When fresh values are requested, the user id is required.")
                    } else {
                        val refreshed = try {
                            refreshGrades(contextBesteschuleAccessToken, contextBesteschuleUserId)
                        } catch (e: Exception) {
                            Logger.e { "Failed to refresh grades: $e" }
                            null
                        }

                        if (refreshed != null) {
                            emit(refreshed)
                        } else {
                            emit(Response.Error.Other("Failed to refresh grades: $refreshed"))
                        }
                    }
                }
            }
                }
        } }

        if (responsePreference != ResponsePreference.Fresh) return constructFlow()

        return getGradesHotFlows.getOrPut(key) {
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
                getGradesHotFlows.remove(key)
            }

            shared
        }
    }

    override fun getGrade(
        gradeId: Int,
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<BesteSchuleGrade>> = flow {
        val dbFlow = vppDatabase.besteSchuleGradesDao.getById(gradeId).map { it?.toModel() }

        dbFlow.collect { cached ->
            val now = Clock.System.now()
            val cacheIsEmpty = cached == null
            val cacheIsStale = cached != null && now - cached.cachedAt > 1.days

            when (responsePreference) {
                ResponsePreference.Fast -> {
                    if (cached != null) emit(Response.Success(cached))

                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null && contextBesteschuleUserId != null) {
                        try {
                            val result = refreshGrade(gradeId, contextBesteschuleAccessToken, contextBesteschuleUserId)
                            if (result is Response.Error.OnlineError.NotFound) {
                                emit(Response.Error.OnlineError.NotFound)
                                return@collect
                            }
                        } catch (_: Exception) {
                        }
                    }
                }

                ResponsePreference.Secure -> {
                    if ((cacheIsEmpty || cacheIsStale) && contextBesteschuleAccessToken != null && contextBesteschuleUserId != null) {
                        val refreshed = try {
                            refreshGrade(gradeId, contextBesteschuleAccessToken, contextBesteschuleUserId)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            emit(refreshed)
                        } else if (cached != null) {
                            emit(Response.Success(cached))
                        } else {
                            emit(Response.Error.Other("Failed to refresh grades and cache is empty"))
                        }
                    } else {
                        if (cached != null) emit(Response.Success(cached))
                        else emit(Response.Error.OnlineError.NotFound)
                    }
                }

                ResponsePreference.Fresh -> {
                    if (contextBesteschuleAccessToken == null) {
                        throw IllegalStateException("When fresh values are requested, a token is required.")
                    } else if (contextBesteschuleUserId == null) {
                        throw IllegalStateException("When fresh values are requested, the user id is required.")
                    } else {
                        val refreshed = try {
                            refreshGrade(gradeId, contextBesteschuleAccessToken, contextBesteschuleUserId)
                        } catch (_: Exception) {
                            null
                        }

                        if (refreshed != null) {
                            if (refreshed is Response.Success) emit(Response.Success(refreshed.data))
                            else emit(Response.Error.OnlineError.NotFound)
                        } else {
                            emit(Response.Error.Other("Failed to refresh grades and cache is empty"))
                        }
                    }
                }
            }
        }
    }

    private suspend fun refreshGrade(
        gradeId: Int,
        accessToken: String,
        userId: Int
    ): Response<BesteSchuleGrade> {
        val apiResponse = getGradeFromApi(gradeId, accessToken)

        if (apiResponse !is Response.Success) return apiResponse as Response.Error

        val now = Clock.System.now()
        val existing = vppDatabase.besteSchuleGradesDao.getAll().first().map { it.toModel() }

        val grade = convertApiGradeToEntity(
            apiGrade = apiResponse.data,
            existingGrades = existing,
            now = now,
            userId = userId
        )

        addGradesToCache(listOf(grade))
        return Response.Success(grade)
    }

    private suspend fun refreshGrades(
        accessToken: String,
        userId: Int
    ): Response<List<BesteSchuleGrade>> {
        val apiResponse = getGradesFromApi(accessToken)
        if (apiResponse !is Response.Success) return apiResponse as Response.Error

        val now = Clock.System.now()
        val existing = vppDatabase.besteSchuleGradesDao.getAll().first().map { it.toModel() }
        val grades = apiResponse.data.map { gradesData ->
            convertApiGradeToEntity(
                apiGrade = gradesData,
                existingGrades = existing,
                now = now,
                userId = userId
            )
        }
        addGradesToCache(grades)
        return Response.Success(grades)
    }

    private fun convertApiGradeToEntity(
        apiGrade: ApiStudentGradesData,
        existingGrades: List<BesteSchuleGrade>,
        now: Instant,
        userId: Int,
    ): BesteSchuleGrade {
        val regexForGradeInParentheses = "\\((.*?)\\)".toRegex()
        val matchResult = regexForGradeInParentheses.find(apiGrade.value)

        val isOptional = matchResult != null
        val value =
            if (matchResult != null) matchResult.groupValues[1]
            else if (apiGrade.value == "-") null
            else apiGrade.value

        if (matchResult != null) matchResult.groupValues[1] else apiGrade.value

        return BesteSchuleGrade(
            id = apiGrade.id,
            value = value,
            isOptional = isOptional,
            isSelectedForFinalGrade = existingGrades.find { it.id == apiGrade.id }?.isSelectedForFinalGrade ?: true,
            schulverwalterUserId = userId,
            collectionId = apiGrade.collection.id,
            givenAt = LocalDate.parse(apiGrade.givenAt),
            cachedAt = now
        )
    }

    override suspend fun clearCacheForUser(schulverwalterUserId: Int) {
        vppDatabase.besteSchuleGradesDao.clearCacheForUser(schulverwalterUserId)
    }
}