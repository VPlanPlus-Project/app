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
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.AssessmentRepository

private val logger = Logger.withTag("AssessmentRepositoryImpl")

class AssessmentRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : AssessmentRepository {
    override suspend fun download(schoolApiAccess: SchoolApiAccess, defaultLessonIds: List<Int>): Response.Error? {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "assessment")
                    parameters {
                        append("filter_default_lessons", defaultLessonIds.joinToString(","))
                    }
                }
                schoolApiAccess.authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse<Any>()
            val assessments = ResponseDataWrapper.fromJson<List<AssessmentGetResponse>>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            assessments.forEach { assessment ->
                vppDatabase.assessmentDao.upsert(
                    DbAssessment(
                        id = assessment.id,
                        createdBy = assessment.createdBy,
                        createdByProfile = null,
                        date = LocalDate.parse(assessment.date),
                        isPublic = assessment.isPublic,
                        defaultLessonId = assessment.subject,
                        description = assessment.description,
                        type = (Assessment.Type.entries.firstOrNull { it.name == assessment.type } ?: Assessment.Type.OTHER).ordinal,
                        createdAt = Instant.fromEpochSeconds(assessment.createdAt)
                    )
                )
            }

            return null
        }
        return Response.Error.Cancelled
    }

    override suspend fun createAssessmentOnline(
        vppId: VppId.Active,
        date: LocalDate,
        type: Assessment.Type,
        defaultLessonId: Int,
        isPublic: Boolean,
        content: String
    ): Response<Int> {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "assessment")
                }
                setBody(AssessmentPostRequest(
                    subjectInstance = defaultLessonId,
                    date = date.toString(),
                    isPublic = isPublic,
                    content = content
                ))
            }

            if (response.status != HttpStatusCode.OK) {
                logger.e { "Error creating assessment: $response" }
                return response.toResponse()
            }

            val id = ResponseDataWrapper.fromJson<Int>(response.bodyAsText())
                ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(id)
        }
        return Response.Error.Cancelled
    }

    override suspend fun getIdForNewLocalAssessment(): Int {
        return (vppDatabase.assessmentDao.getSmallestId() ?: -1).coerceAtMost(-1)
    }

    override suspend fun upsert(assessments: List<Assessment>) {
        vppDatabase.assessmentDao.upsert(
            assessments = assessments.map { DbAssessment(
                id = it.id,
                createdByProfile = (it.creator as? AppEntity.Profile)?.id,
                createdBy = (it.creator as? AppEntity.VppId)?.id,
                createdAt = it.createdAt.toInstant(TimeZone.UTC),
                date = it.date,
                isPublic = it.isPublic,
                defaultLessonId = it.defaultLessonId,
                description = it.description,
                type = it.type.ordinal
            ) }
        )
    }
}

@Serializable
private data class AssessmentGetResponse(
    @SerialName("id") val id: Int,
    @SerialName("subject_instance_id") val subject: Int,
    @SerialName("content") val description: String = "",
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("date") val date: String,
    @SerialName("type") val type: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("created_by") val createdBy: Int,
)

@Serializable
private data class AssessmentPostRequest(
    @SerialName("subject_instance") val subjectInstance: Int,
    @SerialName("date") val date: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("content") val content: String
)