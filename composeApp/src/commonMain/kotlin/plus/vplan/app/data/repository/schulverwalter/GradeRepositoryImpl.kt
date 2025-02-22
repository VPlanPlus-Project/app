package plus.vplan.app.data.repository.schulverwalter

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository

class GradeRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : GradeRepository {
    override suspend fun download(): Response<Set<Int>> {
        safeRequest(onError = { return it }) {
            val accessTokens = vppDatabase.vppIdDao.getSchulverwalterAccess().first()
            val ids = mutableSetOf<Int>()
            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "grades")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@forEach
                val data = ResponseDataWrapper.fromJson<List<GradeItemResponse>>(response.bodyAsText())
                    ?: return@forEach



                ids.addAll(data.map { it.id })
            }
            return Response.Success(ids)
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Grade>> {
        TODO("Not yet implemented")
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.gradeDao.getAll()
}

@Serializable
private data class GradeItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("value") val value: String,
    @SerialName("given_at") val givenAt: String,
    @SerialName("subject") val subject: Subject,
    @SerialName("collection") val collection: Collection,
    @SerialName("teacher") val teacher: Teacher,
) {
    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("name") val name: String,
    )

    @Serializable
    data class Collection(
        @SerialName("id") val id: Int,
        @SerialName("type") val type: String,
        @SerialName("name") val name: String,
        @SerialName("subject_id") val subjectId: Int,
        @SerialName("interval_id") val intervalId: Int,
    )

    @Serializable
    data class Teacher(
        @SerialName("id") val id: Int,
        @SerialName("forename") val forename: String,
        @SerialName("name") val surname: String,
        @SerialName("local_id") val localId: String,
    )
}