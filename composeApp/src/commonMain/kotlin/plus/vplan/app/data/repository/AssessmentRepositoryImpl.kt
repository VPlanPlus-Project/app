@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.DbProfileAssessmentIndex
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.AssessmentRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val logger = Logger.withTag("AssessmentRepositoryImpl")

class AssessmentRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : AssessmentRepository {

    private val onlineChangeRequests = mutableListOf<OnlineChangeRequest>()

    override suspend fun download(schoolApiAccess: VppSchoolAuthentication, subjectInstanceIds: List<Int>): Response<List<Int>> {
        TODO()
    }

    override suspend fun createAssessmentOnline(
        vppId: VppId.Active,
        date: LocalDate,
        type: Assessment.Type,
        subjectInstanceId: Int,
        isPublic: Boolean,
        content: String
    ): Response<Int> {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1")
                }.build())
                setBody(AssessmentPostRequest(
                    subjectInstanceId = subjectInstanceId,
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

            val id = response.body<ResponseDataWrapper<AssessmentPostResponse>>().data.id

            return Response.Success(id)
        }
        return Response.Error.Cancelled
    }

    override suspend fun linkFileToAssessmentOnline(
        vppId: VppId.Active,
        assessmentId: Int,
        fileId: Int
    ): Response.Error? {
        TODO()
    }

    override suspend fun linkFileToAssessment(assessmentId: Int, fileId: Int) {
        vppDatabase.assessmentDao.upsert(FKAssessmentFile(assessmentId, fileId))
    }

    override suspend fun unlinkFileFromAssessment(assessmentId: Int, fileId: Int) {
        vppDatabase.assessmentDao.deleteFileAssessmentConnections(assessmentId, fileId)
    }

    override fun getAll(): Flow<List<Assessment>> {
        return vppDatabase.assessmentDao.getAll().map { it.map { item -> item.toModel() } }
    }

    override fun getByDate(date: LocalDate): Flow<List<Assessment>> {
        return vppDatabase.assessmentDao.getByDate(date).map { it.map { it.toModel() } }
    }

    override fun getByProfile(profileId: Uuid, date: LocalDate?): Flow<List<Assessment>> {
        if (date == null) return vppDatabase.assessmentDao.getByProfile(profileId).map { it.map { it.toModel() } }
        return vppDatabase.assessmentDao.getByProfileAndDate(profileId, date).map { it.map { it.toModel() } }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.assessmentDao.getAll().map { it.map { it.assessment.id } }
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Assessment>> {
        return flowOf(CacheState.Error(id.toString(), Response.Error.Cancelled))
    }

    override suspend fun deleteAssessment(
        assessment: Assessment,
        profile: Profile.StudentProfile
    ): Response.Error? {
        TODO()
    }

    override suspend fun getIdForNewLocalAssessment(): Int {
        return (vppDatabase.assessmentDao.getSmallestId() ?: -1).coerceAtMost(-1)
    }

    override suspend fun upsertLocally(
        assessmentId: Int,
        subjectInstanceId: Int,
        date: LocalDate,
        isPublic: Boolean?,
        createdAt: Instant,
        createdBy: Int?,
        createdByProfile: Uuid?,
        description: String,
        type: Assessment.Type,
        associatedFileIds: List<Int>
    ) {
        vppDatabase.assessmentDao.deleteFileLinks(listOf(assessmentId))
        vppDatabase.assessmentDao.upsertSingleAssessment(
            assessment = DbAssessment(
                id = assessmentId,
                createdBy = createdBy,
                createdByProfile = createdByProfile,
                createdAt = createdAt,
                date = date,
                isPublic = isPublic ?: false,
                subjectInstanceId = subjectInstanceId,
                description = description,
                type = type.ordinal,
                cachedAt = Clock.System.now()
            ),
            fileIds = associatedFileIds
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
                val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "assessment", assessment.id.toString())
                }.build()) {
                    profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
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
                val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "assessment", assessment.id.toString())
                }.build()) {
                    profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
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
                val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "assessment", assessment.id.toString())
                }.build()) {
                    profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
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
                val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "assessment", assessment.id.toString())
                }.build()) {
                    profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
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

    override suspend fun dropIndicesForProfile(profileId: Uuid) {
        vppDatabase.assessmentDao.dropAssessmentsIndexForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, assessmentIds: Collection<Int>) {
        vppDatabase.assessmentDao.upsertAssessmentsIndex(assessmentIds.map { DbProfileAssessmentIndex(it, profileId) })
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
    @SerialName("subject_instance_id") val subjectInstanceId: Int,
    @SerialName("date") val date: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("content") val content: String,
    @SerialName("type") val type: String
)

@Serializable
private data class AssessmentPostResponse(
    @SerialName("id") val id: Int
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