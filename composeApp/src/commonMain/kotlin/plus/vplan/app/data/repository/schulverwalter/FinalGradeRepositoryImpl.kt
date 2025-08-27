@file:OptIn(ExperimentalTime::class)

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterFinalGrade
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterSubjectSchulverwalterFinalGrade
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.FinalGrade
import plus.vplan.app.domain.repository.schulverwalter.FinalGradeRepository
import plus.vplan.app.utils.sendAll
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class FinalGradeRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : FinalGradeRepository {
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
                        pathSegments = listOf("api", "finalgrades")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@forEach
                val data = ResponseDataWrapper.fromJson<List<FinalGradeResponse>>(response.bodyAsText())
                    ?: return Response.Error.ParsingError(response.bodyAsText())

                handleResponse(data, accessToken.schulverwalterUserId)
                ids.addAll(data.map { it.id })
            }
            return Response.Success(ids)
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<FinalGrade>> = channelFlow {
        val finalGradeFlow = vppDatabase.finalGradeDao.getById(id).map { it?.toModel() }
        if (!forceReload) {
            var hadData = false
            sendAll(finalGradeFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
            if (hadData) return@channelFlow
        }
        send(CacheState.Loading(id.toString()))

        safeRequest(onError = { trySend(CacheState.Error(id, it)) }) {
            val existing = vppDatabase.gradeDao.getById(id).first()
            val accessTokens = existing?.let { listOfNotNull(vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(it.grade.userForRequest).first()) }
                ?: vppDatabase.vppIdDao.getSchulverwalterAccess().first()

            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "finalgrades")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse()))
                val data = ResponseDataWrapper.fromJson<List<FinalGradeResponse>>(response.bodyAsText())
                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                handleResponse(data, accessToken.schulverwalterUserId)
            }

            if (finalGradeFlow.first() == null) return@channelFlow send(CacheState.NotExisting(id.toString()))
            return@channelFlow sendAll(getById(id, false))
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.finalGradeDao.getAll()

    private suspend fun handleResponse(data: List<FinalGradeResponse>, userForRequest: Int) {
        val finalGrades = data.filter { it.calculationRule != null }
        vppDatabase.subjectDao.upsert(
            subjects = finalGrades.map { finalGrade ->
                DbSchulverwalterSubject(
                    id = finalGrade.subject.id,
                    name = finalGrade.subject.name,
                    localId = finalGrade.subject.localId,
                    userForRequest = userForRequest,
                    cachedAt = Clock.System.now()
                )
            }
        )

        vppDatabase.finalGradeDao.upsert(
            finalGrades = finalGrades.mapNotNull { finalGrade ->
                DbSchulverwalterFinalGrade(
                    id = finalGrade.id,
                    calculationRule = finalGrade.calculationRule ?: return@mapNotNull null,
                    userForRequest = userForRequest,
                    cachedAt = Clock.System.now()
                )
            },
            subjectCrossovers = finalGrades.map { finalGrade ->
                FKSchulverwalterSubjectSchulverwalterFinalGrade(
                    subjectId = finalGrade.subject.id,
                    finalGradeId = finalGrade.id
                )
            }
        )

        finalGrades.forEach { finalGrade ->
            vppDatabase.finalGradeDao.deleteSchulverwalterSubjectSchulverwalterFinalGrade(finalGrade.id, listOf(finalGrade.subject.id))
        }
    }
}

@Serializable
private data class FinalGradeResponse(
    @SerialName("id") val id: Int,
    @SerialName("calculation_rule") val calculationRule: String? = null,
    @SerialName("subject") val subject: Subject
) {
    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String,
        @SerialName("local_id") val localId: String,
    )
}