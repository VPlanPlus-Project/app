package plus.vplan.app.network.vpp.news

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.application.network.ApiException
import plus.vplan.app.core.model.application.network.NetworkRequestUnsuccessfulException
import plus.vplan.app.network.besteschule.ResponseDataWrapper
import plus.vplan.app.network.vpp.model.IncludedModel
import kotlin.time.Instant

class NewsApiImpl(
    private val httpClient: HttpClient,
): NewsApi {
    override suspend fun getBySchool(school: School.AppSchool): List<NewsDto> {
        try {
            val response = httpClient.get {
                url(URLBuilder().apply {
                    appendPathSegments("app", "v1", "news")
                    parameters.append("school_id", school.toString())
                }.build())

                url(URLBuilder("https://vplan.plus/api/app").apply {
                    appendPathSegments("app", "v1", "news")
                    parameters.append("school_id", "sp24.${school.buildSp24AppAuthentication().sp24SchoolId}.1")
                }.build())

                school.buildSp24AppAuthentication().authentication(this)
            }

            if (!response.status.isSuccess()) throw NetworkRequestUnsuccessfulException(response)

            return response.body<ResponseDataWrapper<List<ApiNewsResponse>>>().data
                .map { it.toDto() }
        } catch (e: Exception) {
            throw ApiException(e, currentCoroutineContext()[CoroutineName]?.name)
        }
    }
}

@Serializable
private data class ApiNewsResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("app_version_from") val versionFrom: Int?,
    @SerialName("app_version_to") val versionTo: Int?,
    @SerialName("visibility_start") val dateFrom: Long,
    @SerialName("visibility_end") val dateTo: Long,
    @SerialName("schools") val schoolIds: List<IncludedModel>,
    @SerialName("author") val author: String
) {
    fun toDto() = NewsDto(
        id = this.id,
        title = this.title,
        content = this.content,
        date = Instant.fromEpochSeconds(createdAt),
        versionFrom = this.versionFrom,
        versionTo = this.versionTo,
        dateFrom = Instant.fromEpochSeconds(dateFrom),
        dateTo = Instant.fromEpochSeconds(dateTo),
        schools = this.schoolIds
            .map { Alias(AliasProvider.Vpp, it.id.toString(), 1) }
            .toSet(),
        author = this.author,
        isRead = false
    )
}