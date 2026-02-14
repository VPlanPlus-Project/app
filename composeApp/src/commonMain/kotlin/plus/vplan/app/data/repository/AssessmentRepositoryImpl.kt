@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbAssessment
import plus.vplan.app.data.source.database.model.database.DbProfileAssessmentIndex
import plus.vplan.app.data.source.database.model.database.foreign_key.FKAssessmentFile
import plus.vplan.app.data.source.network.GenericAuthenticationProvider
import plus.vplan.app.data.source.network.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.data.source.network.model.IncludedModel
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.getFirstValueOld
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.AssessmentRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val logger = Logger.withTag("AssessmentRepositoryImpl")

class AssessmentRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
    private val genericAuthenticationProvider: GenericAuthenticationProvider,
) : AssessmentRepository {

    private val onlineChangeRequests = mutableListOf<OnlineChangeRequest>()

    override suspend fun download(schoolApiAccess: VppSchoolAuthentication, subjectInstanceAliases: List<Alias>): Response<List<Int>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1")
                    if (subjectInstanceAliases.isNotEmpty()) {
                        parameters.append("filter_subject_instances", subjectInstanceAliases.joinToString(","))
                    }
                    parameters.append("include_files", "true")
                }.build())
                schoolApiAccess.authentication(this)
            }

            if (response.status != HttpStatusCode.OK) return response.toErrorResponse()

            val data = response.body<ResponseDataWrapper<List<AssessmentGetResponse>>>().data

            data.forEach { item ->
                upsertApiResponse(item)
            }

            return Response.Success(data.map { it.id })
        }
        return Response.Error.Cancelled
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
        safeRequest(onError = { return it }) {
            val response = httpClient.post(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("assessment", "v1", assessmentId.toString(), "file")
            }.buildString()) {
                vppId.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(AssessmentFileLinkRequest(fileId))
            }

            if (!response.status.isSuccess()) return response.toErrorResponse()
            return null
        }
        return Response.Error.Cancelled
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
        return vppDatabase.assessmentDao.getByDate(date).map { it.map { assessment -> assessment.toModel() } }
    }

    override fun getByProfile(profileId: Uuid, date: LocalDate?): Flow<List<Assessment>> {
        if (date == null) return vppDatabase.assessmentDao.getByProfile(profileId).map { it.map { assessment -> assessment.toModel() } }
        return vppDatabase.assessmentDao.getByProfileAndDate(profileId, date).map { it.map { assessment -> assessment.toModel() } }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.assessmentDao.getAll().map { it.map { assessment -> assessment.assessment.id } }
    }

    private val idAssessmentFlowCache = mutableMapOf<String, Flow<CacheState<Assessment>>>()
    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Assessment>> {
        val cacheKey = "${id}_${forceReload}"
        if (!forceReload) {
            idAssessmentFlowCache[cacheKey]?.let { return it }
        }

        val flow = flow {
            var hasReloaded = false
            vppDatabase.assessmentDao.getById(id).map { it?.toModel() }.collect { assessment ->
                if (assessment == null || (forceReload && !hasReloaded)) {
                    hasReloaded = true
                    emit(CacheState.Loading(id.toString()))
                    val downloadError = downloadById(id)
                    if (downloadError != null) {
                        if (downloadError is Response.Error.OnlineError.NotFound) emit(CacheState.NotExisting(id.toString()))
                        else emit(CacheState.Error(id.toString(), downloadError))
                        return@collect
                    }
                }
                assessment?.let { emit(CacheState.Done(assessment)) }
            }
        }
            .onCompletion { idAssessmentFlowCache.remove(cacheKey) }
            .shareIn(CoroutineScope(Dispatchers.Default), replay = 1, started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000))

        if (!forceReload) idAssessmentFlowCache[cacheKey] = flow
        return flow
    }

    override suspend fun deleteAssessment(
        assessment: Assessment,
        profile: Profile.StudentProfile
    ): Response.Error? {
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active
        if (assessment.id < 0 || vppId == null) {
            vppDatabase.assessmentDao.deleteById(listOf(assessment.id))
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("assessment", "v1", assessment.id.toString())
            }.build()) {
                vppId.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            vppDatabase.assessmentDao.deleteById(listOf(assessment.id))
            return null
        }
        return Response.Error.Cancelled
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
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active
        val oldType = assessment.type
        if (oldType == type) return
        vppDatabase.assessmentDao.updateType(assessment.id, type.ordinal)

        if (assessment.id < 0 || vppId == null) return

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Type }
        val request = OnlineChangeRequest.Type(assessment)
        onlineChangeRequests.add(request)
        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)
            safeRequest(onError = { vppDatabase.assessmentDao.updateType(assessment.id, oldType.ordinal) }) {
                val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1", assessment.id.toString())
                }.build()) {
                    vppId.buildVppSchoolAuthentication().authentication(this)
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
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active
        val oldDate = assessment.date
        if (oldDate == date) return
        vppDatabase.assessmentDao.updateDate(assessment.id, date)

        if (assessment.id < 0 || vppId == null) return

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Date }
        val request = OnlineChangeRequest.Date(assessment)
        onlineChangeRequests.add(request)

        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)

            safeRequest(onError = { vppDatabase.assessmentDao.updateDate(assessment.id, oldDate) }) {
                val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1", assessment.id.toString())
                }.build()) {
                    vppId.buildVppSchoolAuthentication().authentication(this)
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
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active
        if (assessment.id < 0 || vppId == null) return
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
                val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1", assessment.id.toString())
                }.build()) {
                    vppId.buildVppSchoolAuthentication().authentication(this)
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
        val vppId = profile.vppId?.getFirstValueOld() as? VppId.Active
        val oldContent = assessment.description
        if (oldContent == content) return
        vppDatabase.assessmentDao.updateContent(assessment.id, content)

        if (assessment.id < 0 || vppId == null) return

        onlineChangeRequests.removeAll { it.assessment.id == assessment.id && it is OnlineChangeRequest.Content }
        val request = OnlineChangeRequest.Content(assessment)
        onlineChangeRequests.add(request)

        GlobalScope.launch {
            delay(5000)
            if (request !in onlineChangeRequests) return@launch
            onlineChangeRequests.remove(request)

            safeRequest(onError = { vppDatabase.assessmentDao.updateContent(assessment.id, oldContent) }) {
                val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1", assessment.id.toString())
                }.build()) {
                    vppId.buildVppSchoolAuthentication().authentication(this)
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

    private val runningDownloads = mutableMapOf<Int, Deferred<Response.Error?>>()
    private suspend fun downloadById(id: Int): Response.Error? {
        runningDownloads[id]?.let { return it.await() }

        val deferred = CoroutineScope(Dispatchers.Default).async download@{
            try {
                val authenticationOptions = getAuthenticationOptionsForRestrictedEntity(
                    httpClient,
                    URLBuilder(currentConfiguration.appApiUrl).apply { appendPathSegments("assessment", "v1", id.toString()) }.buildString()
                )
                if (authenticationOptions !is Response.Success) return@download authenticationOptions as Response.Error

                val authentication = genericAuthenticationProvider.getAuthentication(authenticationOptions.data)
                if (authentication == null) {
                    return@download Response.Error.Other("No authentication found for school with id ${authenticationOptions.data}")
                }

                val response = httpClient.get(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("assessment", "v1", id.toString())
                }.build()) {
                    authentication.authentication(this)
                }

                if (response.status != HttpStatusCode.OK) {
                    logger.e { "Error downloading assessment with id $id: $response" }
                    return@download response.toErrorResponse()
                }

                val assessment = response.body<ResponseDataWrapper<AssessmentGetResponse>>().data
                upsertApiResponse(assessment)

                return@download null
            } finally {
                runningDownloads.remove(id)
            }
        }
        runningDownloads[id] = deferred
        return deferred.await()
    }

    private suspend fun upsertApiResponse(item: AssessmentGetResponse) {
        upsertLocally(
            assessmentId = item.id,
            subjectInstanceId = item.subject.id,
            date = LocalDate.parse(item.date),
            isPublic = item.isPublic,
            createdAt = Instant.fromEpochSeconds(item.createdAt),
            createdBy = item.createdBy.id,
            createdByProfile = null,
            description = item.description,
            type = Assessment.Type.valueOf(item.type),
            associatedFileIds = item.files.map { it.id }
        )
    }
}

@Serializable
private data class AssessmentGetResponse(
    @SerialName("id") val id: Int,
    @SerialName("subject_instance") val subject: IncludedModel,
    @SerialName("content") val description: String = "",
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("date") val date: String,
    @SerialName("type") val type: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("created_by") val createdBy: IncludedModel,
    @SerialName("files") val files: List<IncludedModel>
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