package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.OnlineSchool
import plus.vplan.app.domain.repository.SchoolRepository

class SchoolRepositoryImpl(
    private val httpClient: HttpClient
) : SchoolRepository {
    override suspend fun fetchAllOnline(): Response<List<OnlineSchool>> {
        return try {
            val response = httpClient.get("https://vplan.plus/api/v2.2/school")
            if (!response.status.isSuccess()) return Response.Error.Other(response.status.toString())
            val data = ResponseDataWrapper.fromJson<List<OnlineSchoolResponse>>(response.bodyAsText()) ?: return Response.Error.ParsingError()
            return Response.Success(data.map { it.toModel() })
        } catch (e: Exception) {
            Response.Error.Other(e.message ?: "Unknown error")
        }
    }
}

@Serializable
private data class OnlineSchoolResponse(
    @SerialName("school_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("sp24_id") val sp24Id: Int? = null
) {
    fun toModel(): OnlineSchool {
        return OnlineSchool(
            id = id,
            name = name,
            sp24Id = sp24Id
        )
    }
}