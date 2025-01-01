package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
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
import plus.vplan.app.utils.sha256

private val logger = Logger.withTag("HomeworkRepositoryImpl")

class HomeworkRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : HomeworkRepository {
    override suspend fun getByDefaultLesson(
        authentication: SchoolApiAccess,
        defaultLessonIds: List<String>,
        from: LocalDateTime?,
        to: LocalDate?
    ): Response<List<HomeworkResponse>> {
        return saveRequest {
            val response = httpClient.get(
                URLBuilder(
                    protocol = VPP_PROTOCOL,
                    host = SERVER_IP,
                    port = VPP_PORT,
                    pathSegments = listOf("api", "v2.2", "school", authentication.schoolId.toString(), "homework"),
                    parameters = Parameters.build {
                        append("filter_until", from?.toString().orEmpty() + ".." + to?.toString().orEmpty())
                        append("filter_default_lessons", defaultLessonIds.joinToString("|"))
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

    override suspend fun upsert(homework: List<Homework>) {
        vppDatabase.homeworkDao.upsertMany(
            homework = homework.map { homeworkItem ->
                DbHomework(
                    id = homeworkItem.id,
                    defaultLessonId = homeworkItem.defaultLesson?.id,
                    groupId = homeworkItem.group?.id,
                    createdAt = homeworkItem.createdAt,
                    createdByProfileId = when (homeworkItem) {
                        is Homework.CloudHomework -> null
                        is Homework.LocalHomework -> homeworkItem.createdByProfile.id
                    },
                    createdBy = when (homeworkItem) {
                        is Homework.CloudHomework -> homeworkItem.createdBy.id
                        is Homework.LocalHomework -> null
                    },
                    isPublic = when (homeworkItem) {
                        is Homework.CloudHomework -> homeworkItem.isPublic
                        is Homework.LocalHomework -> false
                    },
                    dueTo = homeworkItem.dueTo,
                )
            },
            homeworkTask = homework.flatMap { homeworkItem ->
                homeworkItem.tasks.map { homeworkTask ->
                    DbHomeworkTask(
                        id = homeworkTask.id,
                        homeworkId = homeworkItem.id,
                        content = homeworkTask.content
                    )
                }
            }
        )
    }

    override suspend fun getByGroup(groupId: Int): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getAll().map { flowData ->
            flowData.filter {
                it.group?.group?.id == groupId || it.defaultLesson?.groups?.map { group -> group.group.id }?.contains(groupId) ?: false
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

    override suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        group: Group,
        defaultLesson: DefaultLesson?,
        isPublic: Boolean,
        tasks: List<String>
    ): Response<CreateHomeworkResponse> {
        return saveRequest {
            val response = httpClient.post(VPP_ROOT_URL + "/api/v2.2/school/${group.school.id}/homework") {
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
    @SerialName("description") val description: String
)