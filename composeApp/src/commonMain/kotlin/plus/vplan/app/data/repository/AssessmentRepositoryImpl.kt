package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
import plus.vplan.app.data.source.network.isResponseFromBackend
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

    private val onlineChangeRequests = mutableListOf<OnlineChangeRequest>()

    override suspend fun download(schoolApiAccess: SchoolApiAccess, defaultLessonIds: List<Int>): Response<List<Int>> {
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

            vppDatabase.assessmentDao.deleteFileLinks(assessments.map { it.id })
            vppDatabase.assessmentDao.upsert(
                assessments = assessments.map { assessment -> DbAssessment(
                    id = assessment.id,
                    createdBy = assessment.createdBy,
                    createdByProfile = null,
                    createdAt = Instant.fromEpochSeconds(assessment.createdAt),
                    date = LocalDate.parse(assessment.date),
                    isPublic = assessment.isPublic,
                    defaultLessonId = assessment.subject,
                    description = assessment.description,
                    type = (Assessment.Type.entries.firstOrNull { it.name == assessment.type } ?: Assessment.Type.OTHER).ordinal,
                    cachedAt = Clock.System.now()
                ) },
                files = assessments.flatMap { assessment ->
                    assessment.files.map {
                        FKAssessmentFile(assessment.id, it)
                    }
                }
            )

            return Response.Success(assessments.map { it.id })
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
                vppId.buildSchoolApiAccess().authentication(this)
            }
            if (response.status.isSuccess()) return null
            return response.toErrorResponse<Any>()
        }
        return Response.Error.Cancelled
    }

    override suspend fun linkFileToAssessment(assessmentId: Int, fileId: Int) {
        vppDatabase.assessmentDao.upsert(FKAssessmentFile(assessmentId, fileId))
    }

    override fun getAll(): Flow<List<Assessment>> {
        return vppDatabase.assessmentDao.getAll().map { it.map { item -> item.toModel() } }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.assessmentDao.getAll().map { it.map { it.assessment.id } }
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
                if (metadataResponse.status == HttpStatusCode.NotFound && metadataResponse.isResponseFromBackend()) {
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

                vppDatabase.assessmentDao.deleteFileLinks(listOf(id))
                vppDatabase.assessmentDao.upsert(
                    assessments = listOf(DbAssessment(
                        id = id,
                        createdBy = data.createdBy,
                        createdByProfile = null,
                        createdAt = Instant.fromEpochSeconds(data.createdAt),
                        date = LocalDate.parse(data.date),
                        isPublic = data.isPublic,
                        defaultLessonId = data.subject,
                        description = data.description,
                        type = (Assessment.Type.entries.firstOrNull { it.name == data.type } ?: Assessment.Type.OTHER).ordinal,
                        cachedAt = Clock.System.now()
                    )),
                    files = data.files.map { FKAssessmentFile(id, it) }
                )
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
                type = it.type.ordinal,
                cachedAt = it.cachedAt
            ) },
            files = assessments.flatMap { assessment ->
                assessment.files.map { fileId -> FKAssessmentFile(
                    fileId = fileId,
                    assessmentId = assessment.id
                ) }
            }
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun changeType(
        assessment: Assessment,
        type: Assessment.Type,
        profile: Profile.StudentProfile
    ) {
        val oldType = assessment.type
        if (oldType == type) return
        vppDatabase.assessmentDao.updateType(assessment.id, type.ordinal)

        if (assessment.id < 0 || profile.getVppIdItem() == null) return

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Type }
        val request = OnlineChangeRequest.Type(assessment)
        onlineChangeRequests.add(request)
        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)
            safeRequest(onError = { vppDatabase.assessmentDao.updateType(assessment.id, oldType.ordinal) }) {
                val response = httpClient.patch(URLBuilder(
                    protocol = api.protocol,
                    host = api.host,
                    port = api.port,
                    pathSegments = listOf("api", "v2.2", "assessment", assessment.id.toString())
                ).build()) {
                    profile.getVppIdItem()!!.buildSchoolApiAccess().authentication(this)
                    contentType(ContentType.Application.Json)
                    setBody(AssessmentUpdateTypeRequest(type = type.name))
                }
                if (!response.status.isSuccess()) vppDatabase.assessmentDao.updateType(assessment.id, oldType.ordinal)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun changeDate(
        assessment: Assessment,
        date: LocalDate,
        profile: Profile.StudentProfile
    ) {
        val oldDate = assessment.date
        if (oldDate == date) return
        vppDatabase.assessmentDao.updateDate(assessment.id, date)

        if (assessment.id < 0 || profile.getVppIdItem() == null) return

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Date }
        val request = OnlineChangeRequest.Date(assessment)
        onlineChangeRequests.add(request)

        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)

            safeRequest(onError = { vppDatabase.assessmentDao.updateDate(assessment.id, oldDate) }) {
                val response = httpClient.patch(URLBuilder(
                    protocol = api.protocol,
                    host = api.host,
                    port = api.port,
                    pathSegments = listOf("api", "v2.2", "assessment", assessment.id.toString())
                ).build()) {
                    profile.getVppIdItem()!!.buildSchoolApiAccess().authentication(this)
                    contentType(ContentType.Application.Json)
                    setBody(AssessmentUpdateDateRequest(date = date.toString()))
                }
                if (!response.status.isSuccess()) vppDatabase.assessmentDao.updateDate(assessment.id, oldDate)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun changeVisibility(
        assessment: Assessment,
        isPublic: Boolean,
        profile: Profile.StudentProfile
    ) {
        if (assessment.id < 0 || profile.getVppIdItem() == null) return
        val oldIsPublic = assessment.isPublic
        if (oldIsPublic == isPublic) return
        vppDatabase.assessmentDao.updateVisibility(assessment.id, isPublic)

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Visibility }
        val request = OnlineChangeRequest.Visibility(assessment)
        onlineChangeRequests.add(request)

        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)

            safeRequest(onError = { vppDatabase.assessmentDao.updateVisibility(assessment.id, oldIsPublic) }) {
                val response = httpClient.patch(URLBuilder(
                    protocol = api.protocol,
                    host = api.host,
                    port = api.port,
                    pathSegments = listOf("api", "v2.2", "assessment", assessment.id.toString())
                ).build()) {
                    profile.getVppIdItem()!!.buildSchoolApiAccess().authentication(this)
                    contentType(ContentType.Application.Json)
                    setBody(AssessmentUpdateVisibilityRequest(isPublic = isPublic))
                }
                if (!response.status.isSuccess()) vppDatabase.assessmentDao.updateVisibility(assessment.id, oldIsPublic)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun changeContent(
        assessment: Assessment,
        profile: Profile.StudentProfile,
        content: String
    ) {
        val oldContent = assessment.description
        if (oldContent == content) return
        vppDatabase.assessmentDao.updateContent(assessment.id, content)

        if (assessment.id < 0 || profile.getVppIdItem() == null) return

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Content }
        val request = OnlineChangeRequest.Content(assessment)
        onlineChangeRequests.add(request)

        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)

            safeRequest(onError = { vppDatabase.assessmentDao.updateContent(assessment.id, oldContent) }) {
                val response = httpClient.patch(URLBuilder(
                    protocol = api.protocol,
                    host = api.host,
                    port = api.port,
                    pathSegments = listOf("api", "v2.2", "assessment", assessment.id.toString())
                ).build()) {
                    profile.getVppIdItem()!!.buildSchoolApiAccess().authentication(this)
                    contentType(ContentType.Application.Json)
                    setBody(AssessmentUpdateContentRequest(content = content))
                }
                if (!response.status.isSuccess()) vppDatabase.assessmentDao.updateContent(assessment.id, oldContent)
            }
        }
    }

    override suspend fun clearCache() {
        vppDatabase.assessmentDao.clearCache()
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

@Serializable
data class AssessmentUpdateTypeRequest(
    @SerialName("type") val type: String
)

@Serializable
data class AssessmentUpdateDateRequest(
    @SerialName("date") val date: String
)

@Serializable
data class AssessmentUpdateVisibilityRequest(
    @SerialName("is_public") val isPublic: Boolean
)

@Serializable
data class AssessmentUpdateContentRequest(
    @SerialName("content") val content: String
)

private sealed class OnlineChangeRequest(
    val assessment: Assessment,
) {
    class Content(assessment: Assessment): OnlineChangeRequest(assessment)
    class Type(assessment: Assessment): OnlineChangeRequest(assessment)
    class Date(assessment: Assessment): OnlineChangeRequest(assessment)
    class Visibility(assessment: Assessment): OnlineChangeRequest(assessment)
}