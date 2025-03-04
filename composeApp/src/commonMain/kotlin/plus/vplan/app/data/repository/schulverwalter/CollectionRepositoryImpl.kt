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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterYearSchulverwalterInterval
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Collection
import plus.vplan.app.domain.repository.schulverwalter.CollectionRepository
import plus.vplan.app.utils.sendAll

class CollectionRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : CollectionRepository {
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
                        pathSegments = listOf("api", "collections")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@forEach
                val data = ResponseDataWrapper.fromJson<List<CollectionItemResponse>>(response.bodyAsText())
                    ?: return@forEach

                handleResponse(data, accessToken.schulverwalterUserId)

                ids.addAll(data.map { it.id })
            }
            return Response.Success(ids)
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Collection>> = channelFlow {
        val collectionFlow = vppDatabase.collectionDao.getById(id).map { it?.toModel() }
        if (!forceReload) {
            var hadData = false
            sendAll(collectionFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
            if (hadData) return@channelFlow
        }
        send(CacheState.Loading(id.toString()))

        val existing = vppDatabase.collectionDao.getById(id).first()
        val accessTokens = existing?.let { listOfNotNull(vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(it.collection.userForRequest).first()) }
            ?: vppDatabase.vppIdDao.getSchulverwalterAccess().first()

        accessTokens.forEach { accessToken ->
            val response = httpClient.get {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "beste.schule"
                    port = 443
                    pathSegments = listOf("api", "collections")
                }
                bearerAuth(accessToken.schulverwalterAccessToken)
            }
            if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse<Collection>()))
            val data = ResponseDataWrapper.fromJson<CollectionItemResponse>(response.bodyAsText())
                ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

            handleResponse(listOf(data), accessToken.schulverwalterUserId)
        }

        if (collectionFlow.first() == null) return@channelFlow send(CacheState.NotExisting(id.toString()))
        return@channelFlow sendAll(getById(id, false))
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.collectionDao.getAll()

    private suspend fun handleResponse(data: List<CollectionItemResponse>, userForRequest: Int) {
        vppDatabase.intervalDao.upsert(
            intervals = data.map { collection ->
                DbSchulverwalterInterval(
                    id = collection.interval.id,
                    name = collection.interval.name,
                    type = collection.interval.type,
                    from = LocalDate.parse(collection.interval.from),
                    to = LocalDate.parse(collection.interval.to),
                    includedIntervalId = collection.interval.includedIntervalId,
                    userForRequest = userForRequest,
                    cachedAt = Clock.System.now()
                )
            },
            intervalYearCrossovers = data.map { collection ->
                FKSchulverwalterYearSchulverwalterInterval(
                    yearId = collection.interval.year,
                    intervalId = collection.interval.id
                )
            }
        )
        data.forEach { collection ->
            vppDatabase.intervalDao.deleteSchulverwalterYearSchulverwalterInterval(intervalId = collection.interval.id, yearIds = listOf(collection.interval.year))
        }

        vppDatabase.subjectDao.upsert(subjects = data.map { collection ->
            DbSchulverwalterSubject(
                id = collection.subject.id,
                name = collection.subject.name,
                localId = collection.subject.localId,
                userForRequest = userForRequest,
                cachedAt = Clock.System.now()
            )
        })

        vppDatabase.schulverwalterTeacherDao.upsert(teachers = data.map { collection ->
            DbSchulverwalterTeacher(
                id = collection.teacher.id,
                forename = collection.teacher.forename,
                surname = collection.teacher.surname,
                localId = collection.teacher.localId,
                userForRequest = userForRequest,
                cachedAt = Clock.System.now()
            )
        })

        vppDatabase.collectionDao.upsert(
            collections = data.map {
                DbSchulverwalterCollection(
                    id = it.id,
                    type = it.type,
                    name = it.name,
                    userForRequest = userForRequest,
                    givenAt = LocalDate.parse(it.givenAt),
                    cachedAt = Clock.System.now()
                )
            },
            intervalsCrossovers = data.map { collection ->
                FKSchulverwalterCollectionSchulverwalterInterval(
                    collectionId = collection.id,
                    intervalId = collection.interval.id
                )
            },
            subjectsCrossovers = data.map { collection ->
                FKSchulverwalterCollectionSchulverwalterSubject(
                    collectionId = collection.id,
                    subjectId = collection.subject.id
                )
            }
        )

        data.forEach { collection ->
            vppDatabase.collectionDao.deleteSchulverwalterCollectionSchulverwalterInterval(collectionId = collection.id, intervalIds = listOf(collection.interval.id))
            vppDatabase.collectionDao.deleteSchulverwalterCollectionSchulverwalterSubject(collectionId = collection.id, subjectIds = listOf(collection.subject.id))
        }
    }
}

@Serializable
private data class CollectionItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("type") val type: String,
    @SerialName("name") val name: String,
    @SerialName("interval") val interval: Interval,
    @SerialName("given_at") val givenAt: String,
    @SerialName("subject") val subject: Subject,
    @SerialName("teacher") val teacher: Teacher,
) {
    @Serializable
    data class Interval(
        @SerialName("id") val id: Int,
        @SerialName("name") val name: String,
        @SerialName("type") val type: String,
        @SerialName("from") val from: String,
        @SerialName("to") val to: String,
        @SerialName("included_interval_id") val includedIntervalId: Int?,
        @SerialName("year_id") val year: Int,
    )

    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("name") val name: String,
    )

    @Serializable
    data class Teacher(
        @SerialName("id") val id: Int,
        @SerialName("forename") val forename: String,
        @SerialName("name") val surname: String,
        @SerialName("local_id") val localId: String,
    )
}