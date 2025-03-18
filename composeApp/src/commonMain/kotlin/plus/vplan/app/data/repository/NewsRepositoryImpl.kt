package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.network.model.IncludedModel
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.NewsRepository
import kotlin.time.Duration.Companion.minutes

class NewsRepositoryImpl(
    private val httpClient: HttpClient
) : NewsRepository {
    private val newsCache = mutableMapOf<Int, News>()
    private var lastReload = Instant.fromEpochSeconds(0)

    override suspend fun getBySchool(schoolApiAccess: SchoolApiAccess, reload: Boolean): Response<List<News>> {
        if (!reload && lastReload.minus(5.minutes) < Clock.System.now()) return Response.Success(newsCache.values.filter { schoolApiAccess.schoolId in it.schoolIds })
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "school", schoolApiAccess.schoolId.toString(), "news")
                }

                schoolApiAccess.authentication(this)
            }
            if (response.status != HttpStatusCode.OK) return response.toErrorResponse<List<News>>()

            val data = ResponseDataWrapper.fromJson<List<NewsResponse>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            lastReload = Clock.System.now()

            newsCache.putAll(data.map { it.toModel() }.associateBy { it.id })
            return Response.Success(newsCache.values.filter { schoolApiAccess.schoolId in it.schoolIds })
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<News>> {
        TODO("Not yet implemented")
    }

    override fun getAllIds(): Flow<List<Int>> {
        TODO("Not yet implemented")
    }
}

@Serializable
private data class NewsResponse(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("app_version_from") val versionFrom: Int?,
    @SerialName("app_version_to") val versionTo: Int?,
    @SerialName("visibility_start") val dateFrom: Long?,
    @SerialName("visibility_end") val dateTo: Long?,
    @SerialName("schools") val schoolIds: List<IncludedModel>,
    @SerialName("author") val author: String
) {
    fun toModel() = News(
        id = this.id,
        title = this.title,
        content = this.content,
        date = Instant.fromEpochSeconds(this.createdAt),
        versionFrom = this.versionFrom,
        versionTo = this.versionTo,
        dateFrom = this.dateFrom?.let { Instant.fromEpochSeconds(it) },
        dateTo = this.dateTo?.let { Instant.fromEpochSeconds(it) },
        schoolIds = this.schoolIds.map { it.id },
        author = this.author
    )
}