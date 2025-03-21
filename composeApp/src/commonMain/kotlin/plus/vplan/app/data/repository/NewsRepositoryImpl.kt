package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbNews
import plus.vplan.app.data.source.database.model.database.foreign_key.FKNewsSchool
import plus.vplan.app.data.source.network.model.IncludedModel
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.News
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.NewsRepository

class NewsRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : NewsRepository {
    override suspend fun download(schoolApiAccess: SchoolApiAccess): Response<List<Int>> {
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

            vppDatabase.newsDao.upsert(
                news = data.map { DbNews(
                    id = it.id,
                    author = it.author,
                    title = it.title,
                    content = it.content,
                    createdAt = Instant.fromEpochSeconds(it.createdAt),
                    notBefore = Instant.fromEpochSeconds(it.dateFrom),
                    notAfter = Instant.fromEpochSeconds(it.dateTo),
                    notBeforeVersion = it.versionFrom,
                    notAfterVersion = it.versionTo,
                    isRead = false
                ) },
                schools = data.flatMap { news ->
                    news.schoolIds.map {
                        FKNewsSchool(newsId = news.id, schoolId = it.id)
                    }
                }
            )

            return Response.Success(data.map { it.id })
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<News>> {
        TODO("Not yet implemented")
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.newsDao.getAll().map { it.map { n -> n.news.id } }
    override suspend fun getAll(): Flow<List<News>> = vppDatabase.newsDao.getAll().map { it.map { it.toModel() } }
    override suspend fun delete(ids: List<Int>) {
        vppDatabase.newsDao.delete(ids)
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
    @SerialName("visibility_start") val dateFrom: Long,
    @SerialName("visibility_end") val dateTo: Long,
    @SerialName("schools") val schoolIds: List<IncludedModel>,
    @SerialName("author") val author: String
)