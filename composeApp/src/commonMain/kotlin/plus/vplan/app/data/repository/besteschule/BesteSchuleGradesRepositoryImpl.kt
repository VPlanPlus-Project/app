package plus.vplan.app.data.repository.besteschule

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
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
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleGradesRepositoryImpl : BesteSchuleGradesRepository, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()
    private val httpClient by inject<HttpClient>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleGrade?>>()

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

    override fun getGradesFromCache(userId: Int?): Flow<List<BesteSchuleGrade>> {
        return vppDatabase.besteSchuleGradesDao
            .let { if (userId == null) it.getAll() else it.getAllForUser(userId) }
            .map { it.map { grade -> grade.toModel() } }
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

        val grade = BesteSchuleGrade(
            id = apiResponse.data.id,
            value = apiResponse.data.cleanedValue,
            isOptional = apiResponse.data.isOptional,
            isSelectedForFinalGrade = existing.find { it.id == apiResponse.data.id }?.isSelectedForFinalGrade ?: true,
            schulverwalterUserId = userId,
            collectionId = apiResponse.data.collection.id,
            givenAt = LocalDate.parse(apiResponse.data.givenAt),
            cachedAt = now
        )

        addGradesToCache(listOf(grade))
        return Response.Success(grade)
    }

    override suspend fun clearCacheForUser(schulverwalterUserId: Int) = withContext(Dispatchers.IO) {
        vppDatabase.besteSchuleGradesDao.clearCacheForUser(schulverwalterUserId)
    }
}