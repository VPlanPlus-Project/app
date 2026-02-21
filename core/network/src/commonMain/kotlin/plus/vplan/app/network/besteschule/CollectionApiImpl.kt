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

class CollectionApiImpl(
    private val httpClient: HttpClient,
    private val vppIdDao: VppIdDao
): CollectionApi {
    override suspend fun getAll(): List<CollectionDto> {
        val accesses = vppIdDao.getSchulverwalterAccess().first()

        val items = mutableListOf<CollectionDto>()

        for (access in accesses) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "collections")
                    parameters.append("include", "grades.teacher")
                }
                bearerAuth(access.schulverwalterAccessToken)
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            response.body<ResponseDataWrapper<List<ApiCollectionResponse>>>().data
                .map { it.toDto() }
                .let(items::addAll)

        }

        return items
    }

    override suspend fun getById(id: Int): CollectionDto {
        val accesses = vppIdDao.getSchulverwalterAccess().first()

        for (access in accesses) {
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    pathSegments = listOf("api", "collections", id.toString())
                    parameters.append("include", "grades.teacher")
                }
                bearerAuth(access.schulverwalterAccessToken)
            }

            if (response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden) {
                continue
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            return response.body<ResponseDataWrapper<ApiCollectionResponse>>().data.toDto()
        }

        throw Exception("No valid access token found")
    }
}

@Serializable
internal class ApiCollectionResponse(
    @SerialName("id") val id: Int,
    @SerialName("type") val type: String,
    @SerialName("weighting") val weighting: Float,
    @SerialName("name") val name: String,
    @SerialName("given_at") val givenAt: String,
    @SerialName("interval") val interval: ApiIntervalResponse,
    @SerialName("subject") val subject: ApiSubjectResponse,
    @SerialName("teacher") val teacher: ApiTeacherResponse,
) {
    @Serializable
    data class ApiSubjectResponse(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("name") val name: String,
    ) {
        fun toDto() = CollectionDto.SubjectDto(
            id = this.id,
            localId = this.localId,
            name = this.name
        )
    }

    @Serializable
    data class ApiTeacherResponse(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("forename") val forename: String,
        @SerialName("name") val lastname: String,
    ) {
        fun toDto() = CollectionDto.TeacherDto(
            id = this.id,
            localId = this.localId,
            forename = this.forename,
            lastname = this.lastname,
        )
    }

    fun toDto() = CollectionDto(
        id = this.id,
        type = this.type,
        weighting = this.weighting,
        name = this.name,
        givenAt = this.givenAt,
        interval = this.interval.toDto(),
        subject = this.subject.toDto(),
        teacher = this.teacher.toDto(),
    )

}