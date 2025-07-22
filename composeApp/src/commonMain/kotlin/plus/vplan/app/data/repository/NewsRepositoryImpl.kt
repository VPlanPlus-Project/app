package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
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
import plus.vplan.app.utils.sendAll

class NewsRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : NewsRepository {
    override suspend fun download(schoolApiAccess: SchoolApiAccess): Response<List<Int>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "school", schoolApiAccess.schoolId.toString(), "news")
                }.build())
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
                    isRead = vppDatabase.newsDao.getById(it.id).first()?.news?.isRead ?: false
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
        val newsFlow = vppDatabase.newsDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                sendAll(newsFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
                if (hadData) return@channelFlow
            }
            send(CacheState.Loading(id.toString()))
            send(CacheState.NotExisting(id.toString()))

            safeRequest(onError = { send(CacheState.Error(id.toString(), it)) }) {
                TODO("Add API call")
//                val accessResponse = httpClient.get("${api.url}/api/v2.2/room/$id")
//                if (accessResponse.status == HttpStatusCode.NotFound && accessResponse.isResponseFromBackend()) {
//                    vppDatabase.roomDao.deleteById(listOf(id))
//                    return@channelFlow send(CacheState.NotExisting(id.toString()))
//                }
//
//                if (!accessResponse.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), accessResponse.toErrorResponse<Room>()))
//                val accessData = ResponseDataWrapper.fromJson<RoomUnauthenticatedResponse>(accessResponse.bodyAsText())
//                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(accessResponse.bodyAsText())))
//
//                val school = vppDatabase.schoolDao.findById(accessData.schoolId).first()?.toModel()
//                    .let {
//                        if (it is School.IndiwareSchool && !it.credentialsValid) return@channelFlow send(CacheState.Error(id.toString(), Response.Error.Other("no school for room $id")))
//                        it?.getSchoolApiAccess() ?: run {
//                            vppDatabase.roomDao.deleteById(listOf(id))
//                            return@channelFlow send(CacheState.Error(id.toString(), Response.Error.Other("no school for room $id")))
//                        }
//                    }
//
//                val response = httpClient.get("${api.url}/api/v2.2/room/$id") {
//                    school.authentication(this)
//                }
//                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse<Room>()))
//                val data = ResponseDataWrapper.fromJson<RoomItemResponse>(response.bodyAsText())
//                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))
//
//                vppDatabase.roomDao.upsert(
//                    DbRoom(
//                        id = data.id,
//                        schoolId = data.school.id,
//                        name = data.name,
//                        cachedAt = Clock.System.now()
//                    )
//                )
//
//                return@channelFlow sendAll(getById(id, false))
            }
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.newsDao.getAll().map { it.map { n -> n.news.id } }
    override suspend fun getAll(): Flow<List<News>> = vppDatabase.newsDao.getAll().map { newsList -> newsList.map { it.toModel() } }
    override suspend fun delete(ids: List<Int>) {
        vppDatabase.newsDao.delete(ids)
    }

    override suspend fun upsert(news: News) {
        vppDatabase.newsDao.upsert(
            news = listOf(
                DbNews(
                    id = news.id,
                    author = news.author,
                    title = news.title,
                    content = news.content,
                    createdAt = news.date,
                    notBefore = news.dateFrom,
                    notAfter = news.dateTo,
                    notBeforeVersion = news.versionFrom,
                    notAfterVersion = news.versionTo,
                    isRead = news.isRead
                )
            ),
            schools = news.schoolIds.map { FKNewsSchool(newsId = news.id, schoolId = it) }
        )
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