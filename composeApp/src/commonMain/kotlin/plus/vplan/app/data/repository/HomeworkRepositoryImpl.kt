package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.SERVER_IP
import plus.vplan.app.VPP_PORT
import plus.vplan.app.VPP_PROTOCOL
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.HomeworkResponse
import plus.vplan.app.domain.repository.HomeworkTaskResponse

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
        vppDatabase.homeworkDao.upsert(homework.map { homeworkItem ->
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
        })
    }

    override suspend fun getByGroup(groupId: Int): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getAll().map { flowData ->
            flowData.filter {
                it.group?.group?.id == groupId || it.defaultLesson?.groups?.map { it.group.id }?.contains(groupId) ?: false
            }.map { it.toModel() }
        }
    }
}

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
private data class HomeworkTaskResponseItem(
    @SerialName("id") val id: Int,
    @SerialName("description") val description: String
)