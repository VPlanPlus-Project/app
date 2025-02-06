package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.repository.AssessmentRepository
import plus.vplan.app.utils.sendAll

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
                    content = content,
                    type = type.name
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

    override suspend fun linkFileToAssessmentOnline(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    ): Response.Error? {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "assessment", assessmentId.toString(), "file")
                }
                contentType(ContentType.Application.Json)
                setBody(AssessmentFileLinkRequest(fileId))
            }
            if (response.status.isSuccess()) return null
            return response.toErrorResponse<Any>()
        }
        return Response.Error.Cancelled
    }

    override fun getAll(): Flow<List<Assessment>> {
        return vppDatabase.assessmentDao.getAll().map { it.map { item -> item.toModel() } }
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Assessment>> {
        if (id < 0) {
            return vppDatabase.assessmentDao.getById(id).map {
                if (it == null) CacheState.NotExisting(id.toString())
                else CacheState.Done(it.toModel())
            }
        }

        return channelFlow {
            var canSend = true
            if (!forceReload) {
                var hadData = false
                vppDatabase.assessmentDao.getById(id)
                    .takeWhile { it != null && canSend }
                    .filterNotNull()
                    .onEach { hadData = true; trySend(CacheState.Done(it.toModel())).onClosed { canSend = false } }
                    .collect()
                if (hadData || !canSend) return@channelFlow
            }
            safeRequest(
                onError = { trySend(CacheState.Error(id.toString(), it)); return@channelFlow }
            ) {
                trySend(CacheState.Loading(id.toString())).onFailure { return@channelFlow }
                val metadataResponse = httpClient.get("${api.url}/api/v2.2/assessment/$id")
                if (metadataResponse.status == HttpStatusCode.NotFound) {
                    trySend(CacheState.NotExisting(id.toString()))
                    vppDatabase.assessmentDao.deleteById(listOf(id))
                    return@channelFlow
                }

                if (metadataResponse.status != HttpStatusCode.OK) {
                    trySend(CacheState.Error(id.toString(), metadataResponse.toErrorResponse<Any>()))
                    return@channelFlow
                }

                val metadataResponseData = ResponseDataWrapper.fromJson<AssessmentMetadataResponse>(metadataResponse.bodyAsText())
                    ?: run {
                        trySend(CacheState.Error(id.toString(), Response.Error.ParsingError(metadataResponse.bodyAsText())))
                        return@channelFlow
                    }

                val vppId = vppDatabase.vppIdDao.getById(metadataResponseData.createdBy).first()?.toModel() as? VppId.Active
                val school = vppDatabase.schoolDao.getAll().first().firstOrNull { it.school.id in metadataResponseData.schoolIds }?.toModel()
                    ?: run {
                        trySend(CacheState.NotExisting(id.toString()))
                        return@channelFlow
                    }

                val assessmentResponse = httpClient.get("${api.url}/api/v2.2/assessment/$id") {
                    vppId?.let { bearerAuth(it.accessToken) } ?: school.getSchoolApiAccess()?.authentication(this)
                }
                if (assessmentResponse.status != HttpStatusCode.OK) {
                    trySend(CacheState.Error(id.toString(), metadataResponse.toErrorResponse<Any>()))
                    return@channelFlow
                }
                val data = ResponseDataWrapper.fromJson<AssessmentGetResponse>(assessmentResponse.bodyAsText())
                    ?: run {
                        trySend(CacheState.Error(id.toString(), Response.Error.ParsingError(assessmentResponse.bodyAsText())))
                        return@channelFlow
                    }

                vppDatabase.assessmentDao.upsert(DbAssessment(
                    id = id,
                    createdBy = data.createdBy,
                    createdByProfile = null,
                    createdAt = Instant.fromEpochSeconds(data.createdAt),
                    date = LocalDate.parse(data.date),
                    isPublic = data.isPublic,
                    defaultLessonId = data.subject,
                    description = data.description,
                    type = (Assessment.Type.entries.firstOrNull { it.name == data.type } ?: Assessment.Type.OTHER).ordinal
                ))
                sendAll(getById(id, false))
            }
        }
    }

    override suspend fun deleteAssessment(
        assessment: Assessment,
        profile: Profile.StudentProfile
    ): Response.Error? {
        if (assessment.id < 0 || profile.getVppIdItem() == null) {
            vppDatabase.assessmentDao.deleteById(listOf(assessment.id))
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete(
                URLBuilder(
                protocol = api.protocol,
                host = api.host,
                port = api.port,
                pathSegments = listOf("api", "v2.2", "assessment", assessment.id.toString())
            ).build()) {
                profile.getVppIdItem()!!.buildSchoolApiAccess().authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse<Any>()
            vppDatabase.assessmentDao.deleteById(listOf(assessment.id))
            return null
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
            ) },
            files = assessments.flatMap { assessment ->
                assessment.files.map { fileId -> FKAssessmentFile(
                    fileId = fileId,
                    assessmentId = assessment.id
                ) }
            }
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
    @SerialName("files") val files: List<Int>
)

@Serializable
private data class AssessmentPostRequest(
    @SerialName("subject_instance") val subjectInstance: Int,
    @SerialName("date") val date: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("content") val content: String,
    @SerialName("type") val type: String
)

@Serializable
private data class AssessmentMetadataResponse(
    @SerialName("school_ids") val schoolIds: List<Int>,
    @SerialName("created_by") val createdBy: Int
)

@Serializable
data class AssessmentFileLinkRequest(
    @SerialName("file_id") val fileId: Int,
)