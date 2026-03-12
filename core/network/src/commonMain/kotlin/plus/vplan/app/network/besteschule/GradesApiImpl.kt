package plus.vplan.app.network.besteschule

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.database.dao.VppIdDao
import plus.vplan.app.network.ApiException
import plus.vplan.app.network.NetworkRequestUnsuccessfulException

class GradesApiImpl(
    private val httpClient: HttpClient,
    private val vppIdDao: VppIdDao,
): GradesApi {
    override suspend fun getAll(): List<GradesDto> {
        try {
            val accesses = vppIdDao.getSchulverwalterAccess().first()
            val items = mutableListOf<GradesDto>()

            for (access in accesses) {
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        pathSegments = listOf("api", "grades")
                    }

                    bearerAuth(access.schulverwalterAccessToken)
                }

                if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

                response.body<ResponseDataWrapper<List<ApiGradeResponse>>>().data
                    .map { it.toDto(access.schulverwalterUserId) }
                    .let(items::addAll)
            }

            return items.distinctBy { it.id }
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun getAllForUser(userId: Int): List<GradesDto> {
        try {
            val access = vppIdDao.getSchulverwalterAccess().first().firstOrNull { it.schulverwalterUserId == userId }!!

            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "grades")
                    parameters.append("include", "collection")
                }

                bearerAuth(access.schulverwalterAccessToken)
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            return response.body<ResponseDataWrapper<List<ApiGradeResponse>>>().data
                .map { it.toDto(access.schulverwalterUserId) }
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }

    override suspend fun getById(id: Int): GradesDto? {
        try {
            val accesses = vppIdDao.getSchulverwalterAccess().first()

            for (access in accesses) {
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        pathSegments = listOf("api", "grades", id.toString())
                        parameters.append("include", "collection")
                    }

                    bearerAuth(access.schulverwalterAccessToken)
                }

                if (response.status in setOf(
                        HttpStatusCode.Unauthorized,
                        HttpStatusCode.Forbidden,
                        HttpStatusCode.NotFound
                    )) continue

                if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

                val grade = response.body<ResponseDataWrapper<ApiGradeResponse>>().data

                return grade.toDto(access.schulverwalterUserId)
            }

            return null
        } catch (e: Exception) {
            throw ApiException(e)
        }
    }
}

@Serializable
private data class ApiGradeResponse(
    @SerialName("id") val id: Int,
    @SerialName("value") val value: String,
    @SerialName("read") val read: Boolean,
    @SerialName("given_at") val givenAt: String,
    @SerialName("subject") val subject: Subject,
    @SerialName("teacher") val teacher: Teacher,
    @SerialName("collection") val collection: Collection,
) {
    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("name") val name: String,
    ) {
        fun toDto() = GradesDto.Subject(
            id = this.id,
            localId = this.localId,
            name = this.name
        )
    }

    @Serializable
    data class Teacher(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("forename") val forename: String,
        @SerialName("name") val lastname: String,
    ) {
        fun toDto() = GradesDto.Teacher(
            id = this.id,
            localId = this.localId,
            forename = this.forename,
            lastname = this.lastname,
        )
    }

    @Serializable
    data class Collection(
        @SerialName("id") val id: Int,
        @SerialName("type") val type: String,
        @SerialName("weighting") val weighting: Float,
        @SerialName("name") val name: String,
        @SerialName("given_at") val givenAt: String,
        @SerialName("subject_id") val subjectId: Int,
        @SerialName("teacher_id") val teacherId: Int,
        @SerialName("interval_id") val intervalId: Int,
    ) {
        fun toDto() = GradesDto.Collection(
            id = this.id,
            type = this.type,
            weighting = this.weighting,
            name = this.name,
            givenAt = this.givenAt,
            subjectId = this.subjectId,
            teacherId = this.teacherId,
            intervalId = this.intervalId,
        )
    }

    fun toDto(userId: Int) = GradesDto(
        id = this.id,
        value = this.value,
        read = this.read,
        givenAt = this.givenAt,
        subject = this.subject.toDto(),
        teacher = this.teacher.toDto(),
        collection = this.collection.toDto(),
        schulverwalterUserId = userId,
    )
}