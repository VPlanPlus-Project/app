package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.SERVER_IP
import plus.vplan.app.VPP_PORT
import plus.vplan.app.VPP_PROTOCOL
import plus.vplan.app.VPP_ROOT_URL
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkFile
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.CreateHomeworkResponse
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.HomeworkResponse
import plus.vplan.app.domain.repository.HomeworkTaskResponse
import plus.vplan.app.feature.homework.ui.components.File
import plus.vplan.app.utils.sha256

private val logger = Logger.withTag("HomeworkRepositoryImpl")

class HomeworkRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
) : HomeworkRepository {
    override suspend fun upsert(homework: List<Homework>, tasks: List<Homework.HomeworkTask>, files: List<Homework.HomeworkFile>) {
        vppDatabase.homeworkDao.upsertMany(
            homework = homework.map { homeworkItem ->
                DbHomework(
                    id = homeworkItem.id,
                    defaultLessonId = homeworkItem.defaultLesson,
                    groupId = homeworkItem.group,
                    createdAt = homeworkItem.createdAt,
                    createdByProfileId = when (homeworkItem) {
                        is Homework.CloudHomework -> null
                        is Homework.LocalHomework -> homeworkItem.createdByProfile
                    },
                    createdBy = when (homeworkItem) {
                        is Homework.CloudHomework -> homeworkItem.createdBy
                        is Homework.LocalHomework -> null
                    },
                    isPublic = when (homeworkItem) {
                        is Homework.CloudHomework -> homeworkItem.isPublic
                        is Homework.LocalHomework -> false
                    },
                    dueTo = homeworkItem.dueTo,
                )
            },
            homeworkTask = tasks.map { homeworkTask ->
                DbHomeworkTask(
                    id = homeworkTask.id,
                    homeworkId = homeworkTask.homework,
                    content = homeworkTask.content
                )
            },
            homeworkTaskDoneAccount = emptyList(),
            files = files.map {
                DbHomeworkFile(
                    id = it.id,
                    homeworkId = it.homework,
                    size = it.size,
                    fileName = it.name
                )
            }
        )
    }

    override suspend fun getByGroup(groupId: Int): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getAll().map { flowData ->
            val defaultLessons = vppDatabase.defaultLessonDao.getByGroup(groupId).first()
            flowData.filter {
                it.homework.groupId == groupId || defaultLessons.any { defaultLesson -> defaultLesson.groups.any { group -> group.groupId == groupId } }
            }.map { it.toModel() }
        }
    }

    override suspend fun getByGroup(authentication: SchoolApiAccess, groupId: Int, from: LocalDateTime?, to: LocalDate?): Response<List<HomeworkResponse>> {
        return saveRequest {
            val response = httpClient.get(
                URLBuilder(
                    protocol = VPP_PROTOCOL,
                    host = SERVER_IP,
                    port = VPP_PORT,
                    pathSegments = listOf("api", "v2.2", "school", authentication.schoolId.toString(), "homework"),
                    parameters = Parameters.build {
                        append("filter_until", from?.toString().orEmpty() + ".." + to?.toString().orEmpty())
                        append("filter_group", groupId.toString())
                    }
                ).build()
            ) {
                authentication.authentication(this)
            }
            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error getting homework: $response" }
                return response.toResponse()
            }

            val data = ResponseDataWrapper.fromJson<List<HomeworkResponseItem>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(data.map { homeworkResponseItem ->
                HomeworkResponse(
                    id = homeworkResponseItem.id,
                    createdBy = homeworkResponseItem.createdBy,
                    createdAt = Instant.fromEpochSeconds(homeworkResponseItem.createdAt),
                    dueTo = Instant.fromEpochSeconds(homeworkResponseItem.dueTo),
                    isPublic = homeworkResponseItem.isPublic,
                    group = homeworkResponseItem.group,
                    defaultLesson = homeworkResponseItem.defaultLesson,
                    tasks = homeworkResponseItem.tasks.map { homeworkTaskResponseItem ->
                        HomeworkTaskResponse(
                            id = homeworkTaskResponseItem.id,
                            content = homeworkTaskResponseItem.description
                        )
                    }
                )
            })
        }
    }

    override fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return vppDatabase.homeworkDao.getTaskById(id).map { it?.toModel() }.map { if (it == null) CacheState.NotExisting(id.toString()) else CacheState.Done(it) }
    }

    override fun getAll(): Flow<List<CacheState<Homework>>> {
        return vppDatabase.homeworkDao.getAll().map { it.map { CacheState.Done(it.toModel()) } }
    }

    override fun getById(id: Int): Flow<CacheState<Homework>> {
        if (id < 0) return vppDatabase.homeworkDao.getById(id).map {
            if (it == null) CacheState.NotExisting(id.toString())
            else CacheState.Done(it.toModel())
        }
        return flow {
            val databaseObject = vppDatabase.homeworkDao.getById(id)
            if (databaseObject.first() != null) return@flow emitAll(databaseObject.map {
                if (it == null) CacheState.NotExisting(id.toString())
                else CacheState.Done(it.toModel())
            })

            safeRequest(
                onError = { return@flow emit(CacheState.Error(id.toString(), it)) }
            ) {
                val metadataResponse = httpClient.get("$VPP_ROOT_URL/api/v2.2/homework/$id")
                if (metadataResponse.status == HttpStatusCode.NotFound) return@flow emit(CacheState.NotExisting(id.toString()))
                if (metadataResponse.status != HttpStatusCode.OK) return@flow emit(CacheState.Error(id.toString(), metadataResponse.toErrorResponse<Homework>()))

                val metadataResponseData = ResponseDataWrapper.fromJson<HomeworkMetadataResponse>(metadataResponse.bodyAsText())
                    ?: return@flow emit(CacheState.Error(id.toString(), Response.Error.ParsingError(metadataResponse.bodyAsText())))

                val vppId = vppDatabase.vppIdDao.getById(metadataResponseData.createdBy).first()?.toModel() as? VppId.Active
                val school = vppDatabase.schoolDao.findById(metadataResponseData.schoolId).first()?.toModel()

                val homeworkResponse = httpClient.get("$VPP_ROOT_URL/api/v2.2/homework/$id") {
                    vppId?.let { bearerAuth(it.accessToken) } ?: school?.getSchoolApiAccess()?.authentication(this)
                }
                if (homeworkResponse.status != HttpStatusCode.OK) return@flow emit(CacheState.Error(id.toString(), metadataResponse.toErrorResponse<Homework>()))
                val data = ResponseDataWrapper.fromJson<HomeworkResponseItem>(homeworkResponse.bodyAsText())
                    ?: return@flow emit(CacheState.Error(id.toString(), Response.Error.ParsingError(homeworkResponse.bodyAsText())))

                vppDatabase.homeworkDao.upsertMany(
                    homework = listOf(
                        DbHomework(
                            id = id,
                            defaultLessonId = data.defaultLesson,
                            groupId = data.group,
                            createdAt = Instant.fromEpochSeconds(data.createdAt),
                            dueTo = Instant.fromEpochSeconds(data.dueTo),
                            createdBy = data.createdBy,
                            createdByProfileId = null,
                            isPublic = data.isPublic
                        )),
                    homeworkTask = data.tasks.map { task ->
                        DbHomeworkTask(
                            id = task.id,
                            content = task.description,
                            homeworkId = id
                        )
                    },
                    homeworkTaskDoneAccount = if (vppId == null) emptyList() else data.tasks.mapNotNull {
                        DbHomeworkTaskDoneAccount(
                            taskId = it.id,
                            vppId = vppId.id,
                            isDone = it.done ?: return@mapNotNull null
                        )
                    },
                    files = emptyList() // TODO
                )
            }

            return@flow emitAll(getById(id))
        }
    }

    override suspend fun deleteById(id: Int) {
        deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<Int>) {
        vppDatabase.homeworkDao.deleteById(ids)
    }

    override suspend fun getIdForNewLocalHomework(): Int {
        return (vppDatabase.homeworkDao.getMinId().first() ?: 0).coerceAtMost(-1)
    }

    override suspend fun getIdForNewLocalHomeworkTask(): Int {
        return (vppDatabase.homeworkDao.getMinTaskId().first() ?: 0).coerceAtMost(-1)
    }

    override suspend fun getIdForNewLocalHomeworkFile(): Int {
        return (vppDatabase.homeworkDao.getMinFileId().first() ?: 0).coerceAtMost(-1)
    }

    override suspend fun download(schoolApiAccess: SchoolApiAccess, groupId: Int, defaultLessonIds: List<String>): Response<List<Int>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get(URLBuilder(
                protocol = VPP_PROTOCOL,
                host = SERVER_IP,
                port = VPP_PORT,
                pathSegments = listOf("api", "v2.2", "homework"),
                parameters = Parameters.build {
                    append("filter_groups", groupId.toString())
                    append("filter_default_lessons", defaultLessonIds.joinToString(","))
                }
            ).build()) { schoolApiAccess.authentication(this) }

            if (response.status != HttpStatusCode.OK) return response.toErrorResponse<Any>()

            val data = ResponseDataWrapper.fromJson<List<HomeworkGetResponse>>(response.bodyAsText()) ?: return Response.Error.ParsingError(response.bodyAsText())

            vppDatabase.homeworkDao.upsertMany(
                homework = data.map { homework ->
                    DbHomework(
                        id = homework.id,
                        defaultLessonId = homework.defaultLesson,
                        groupId = if (homework.defaultLesson == null) homework.group else null,
                        createdAt = Instant.fromEpochSeconds(homework.createdAt),
                        dueTo = Instant.fromEpochSeconds(homework.dueTo),
                        createdBy = homework.createdBy,
                        createdByProfileId = null,
                        isPublic = homework.isPublic
                    )
                }.also { Logger.d { "${it.size} homework upserted" } },
                homeworkTask = data.map { homework ->
                    homework.tasks.map { homeworkTask ->
                        DbHomeworkTask(
                            id = homeworkTask.id,
                            homeworkId = homework.id,
                            content = homeworkTask.content
                        )
                    }
                }.flatten(),
                homeworkTaskDoneAccount = if (schoolApiAccess !is SchoolApiAccess.VppIdAccess) emptyList() else data.flatMap { homework ->
                    homework.tasks.mapNotNull {
                        DbHomeworkTaskDoneAccount(
                            taskId = it.id,
                            vppId = schoolApiAccess.id,
                            isDone = it.done ?: return@mapNotNull null
                        )
                    }
                },
                emptyList()
            )

            return Response.Success(data.map { it.id })
        }
        return Response.Error.Cancelled
    }

    override suspend fun clearCache() {
        vppDatabase.homeworkDao.deleteCache()
    }

    override suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        group: Group,
        defaultLesson: DefaultLesson?,
        isPublic: Boolean,
        tasks: List<String>
    ): Response<CreateHomeworkResponse> {
        return saveRequest {
            val response = httpClient.post(VPP_ROOT_URL + "/api/v2.2/school/${group.school}/homework") {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(
                    HomeworkPostRequest(
                        defaultLesson = defaultLesson?.id,
                        groupId = group.id,
                        dueTo = until.toEpochDays() * 24 * 60 * 60L,
                        isPublic = isPublic,
                        tasks = tasks
                    )
                )
            }

            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error creating homework: $response" }
                return response.toResponse()
            }

            val data = ResponseDataWrapper.fromJson<HomeworkPostResponse>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(
                CreateHomeworkResponse(
                    id = data.id,
                    taskIds = tasks.associateWith { task -> data.tasks.first { it.descriptionHash.lowercase() == task.sha256().lowercase() }.id }
                )
            )
        }
    }

    override suspend fun uploadHomeworkDocument(vppId: VppId.Active, homeworkId: Int, document: File): Response<Int> {
        safeRequest(onError = {return it}) {
            val response = httpClient.post("$VPP_ROOT_URL/api/v2.2/homework/$homeworkId/documents") {
                header("File-Name", document.name)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                header(HttpHeaders.ContentLength, document.size.toString())
                bearerAuth(vppId.accessToken)
                setBody(ByteReadChannel(document.platformFile.readBytes()))
            }
            if (response.status != HttpStatusCode.OK) return response.toErrorResponse<Int>()
            return ResponseDataWrapper.fromJson<Int>(response.bodyAsText())?.let { Response.Success(it) } ?: Response.Error.ParsingError(response.bodyAsText())
        }
        return Response.Error.Cancelled
    }
}

@Serializable
data class HomeworkPostRequest(
    @SerialName("default_lesson") val defaultLesson: String? = null,
    @SerialName("group_id") val groupId: Int? = null,
    @SerialName("due_to") val dueTo: Long,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("tasks") val tasks: List<String>,
)

@Serializable
private data class HomeworkResponseItem(
    @SerialName("id") val id: Int,
    @SerialName("created_by") val createdBy: Int,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("due_to") val dueTo: Long,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("group_id") val group: Int?,
    @SerialName("default_lesson") val defaultLesson: String?,
    @SerialName("tasks") val tasks: List<HomeworkTaskResponseItem>,
)

@Serializable
private data class HomeworkPostResponse(
    @SerialName("id") val id: Int,
    @SerialName("tasks") val tasks: List<HomeworkPostResponseItem>,
)

@Serializable
private data class HomeworkPostResponseItem(
    @SerialName("id") val id: Int,
    @SerialName("description_hash") val descriptionHash: String,
)

@Serializable
private data class HomeworkTaskResponseItem(
    @SerialName("id") val id: Int,
    @SerialName("description") val description: String,
    @SerialName("is_done") val done: Boolean?
)

@Serializable
private data class HomeworkMetadataResponse(
    @SerialName("school_id") val schoolId: Int,
    @SerialName("created_by") val createdBy: Int
)

@Serializable
data class HomeworkGetResponse(
    @SerialName("id") val id: Int,
    @SerialName("created_by") val createdBy: Int,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("due_to") val dueTo: Long,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("group") val group: Int,
    @SerialName("default_lesson") val defaultLesson: String?,
    @SerialName("tasks") val tasks: List<HomeworkGetResponseTask>,
)

@Serializable
data class HomeworkGetResponseTask(
    @SerialName("id") val id: Int,
    @SerialName("content") val content: String,
    @SerialName("done") val done: Boolean?
)