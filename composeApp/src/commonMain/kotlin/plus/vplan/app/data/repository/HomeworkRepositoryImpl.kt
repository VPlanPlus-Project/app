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
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.shareIn
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.CacheState
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.model.VppSchoolAuthentication
import plus.vplan.app.core.model.asSuccess
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.data.source.database.model.database.DbProfileHomeworkIndex
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.data.source.network.GenericAuthenticationProvider
import plus.vplan.app.data.source.network.getAuthenticationOptionsForRestrictedEntity
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.data_structure.ConcurrentMutableMap
import plus.vplan.app.domain.model.populated.PopulatedHomework
import plus.vplan.app.domain.repository.CreateHomeworkResponse
import plus.vplan.app.domain.repository.HomeworkEntity
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.utils.sha256
import kotlin.uuid.Uuid

private val logger = Logger.withTag("HomeworkRepositoryImpl")

class HomeworkRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
    private val genericAuthenticationProvider: GenericAuthenticationProvider,
) : HomeworkRepository {
    override suspend fun upsert(homeworkEntity: HomeworkEntity) {
        vppDatabase.homeworkDao.upsertSingleHomework(
            homework = DbHomework(
                id = homeworkEntity.id,
                groupId = homeworkEntity.groupId,
                createdAt = homeworkEntity.createdAt,
                subjectInstanceId = homeworkEntity.subjectInstanceId,
                dueTo = homeworkEntity.dueTo,
                createdBy = homeworkEntity.createdByVppId,
                createdByProfileId = homeworkEntity.createdByProfileId,
                isPublic = homeworkEntity.isPublic,
                cachedAt = homeworkEntity.cachedAt
            ),
            tasks = homeworkEntity.tasks.map { taskEntity ->
                DbHomeworkTask(
                    id = taskEntity.id,
                    homeworkId = taskEntity.homeworkId,
                    content = taskEntity.content,
                    cachedAt = taskEntity.cachedAt
                )
            },
            tasksDoneAccount = emptyList(),
            tasksDoneProfile = emptyList(),
            fileIds = emptyList()
        )
    }

    override fun getByLocalId(id: Int): Flow<Homework?> {
        return vppDatabase.homeworkDao.getById(id).map { it?.toModel() }
    }

    override fun getTaskByLocalId(id: Int): Flow<Homework.HomeworkTask?> {
        return vppDatabase.homeworkDao.getTaskById(id).map { it?.toModel() }
    }

    override fun getByGroup(group: Group): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getAll().map { flowData ->
            val subjectInstances = vppDatabase.subjectInstanceDao.getByGroup(group.id).first()
            flowData.filter {
                it.homework.groupId == group.id || subjectInstances.any { subjectInstance -> subjectInstance.groups.any { siGroup -> siGroup.groupId == group.id } }
            }.map { it.toModel() }
        }
    }

    override fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return vppDatabase.homeworkDao.getTaskById(id).map { it?.toModel() }.map { if (it == null) CacheState.NotExisting(id.toString()) else CacheState.Done(it) }
    }

    override fun getAll(): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getAll().map { it.map { embeddedHomework -> embeddedHomework.toModel() } }
    }

    override fun getByDate(date: LocalDate): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getByDate(date).map { it.map { homework -> homework.toModel() } }
    }

    override fun getByProfile(profileId: Uuid, date: LocalDate?): Flow<List<Homework>> {
        if (date == null) return vppDatabase.homeworkDao.getByProfile(profileId).map { it.map { homework -> homework.toModel() } }
        return vppDatabase.homeworkDao.getByProfileAndDate(profileId, date).map { it.map { homework -> homework.toModel() } }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.homeworkDao.getAll().map { it.map { homework -> homework.homework.id } }
    }

    private val idHomeworkFlowCache = mutableMapOf<String, Flow<CacheState<Homework>>>()
    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Homework>> {
        val cacheKey = "${id}_${forceReload}"
        if (!forceReload) {
            idHomeworkFlowCache[cacheKey]?.let { return it }
        }

        val flow = flow {
            var hasReloaded = false
            vppDatabase.homeworkDao.getById(id).map { it?.toModel() }.collect { homework ->
                if (homework == null || (forceReload && !hasReloaded)) {
                    hasReloaded = true
                    emit(CacheState.Loading(id.toString()))
                    val downloadError = downloadById(id)
                    if (downloadError != null) {
                        if (downloadError is Response.Error.OnlineError.NotFound) emit(CacheState.NotExisting(id.toString()))
                        else emit(CacheState.Error(id, downloadError))
                        return@collect
                    }
                }
                homework?.let { emit(CacheState.Done(homework)) }
            }
        }
            .onCompletion { idHomeworkFlowCache.remove(cacheKey) }
            .shareIn(CoroutineScope(Dispatchers.IO), replay = 1, started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000))

        if (!forceReload) idHomeworkFlowCache[cacheKey] = flow
        return flow
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

    override suspend fun toggleHomeworkTaskDone(task: Homework.HomeworkTask, profile: Profile.StudentProfile) {
        val oldState = task.isDone(profile)
        val newState = !oldState

        if (profile.vppId == null || task.id < 0) {
            vppDatabase.homeworkDao.upsertTaskDoneProfile(DbHomeworkTaskDoneProfile(task.id, profile.id, newState))
            return
        }
        vppDatabase.homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, profile.vppId!!.id, newState))
        safeRequest(onError = { vppDatabase.homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, profile.vppId!!.id, oldState)) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("homework", "v1", task.homeworkId.toString(), "task", task.id.toString())
            }.build()) {
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkTaskUpdateDoneStateRequest(isDone = newState))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, profile.vppId!!.id, oldState))
        }
    }

    override suspend fun editHomeworkSubjectInstance(homework: PopulatedHomework, subjectInstance: SubjectInstance?, group: Group?, profile: Profile.StudentProfile) {
        require((subjectInstance == null) xor (group == null)) { "Either subjectInstance or group must not be null" }
        val oldSubjectInstance = homework.subjectInstance
        val oldSubjectInstanceVppId = oldSubjectInstance?.aliases?.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()

        val oldGroup = homework.group
        val oldGroupVppId = oldGroup?.aliases?.firstOrNull { it.provider == AliasProvider.Vpp }?.value?.toInt()

        val subjectInstanceVppId = subjectInstance?.aliases?.first { it.provider == AliasProvider.Vpp }?.value?.toInt()
        val groupVppId = group?.aliases?.first { it.provider == AliasProvider.Vpp }?.value?.toInt()

        vppDatabase.homeworkDao.updateSubjectInstanceAndGroup(homework.homework.id, subjectInstanceVppId, groupVppId)

        if (homework.homework.id < 0 || profile.vppId == null || homework !is PopulatedHomework.CloudHomework) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateSubjectInstanceAndGroup(homework.homework.id, subjectInstanceVppId, oldGroupVppId) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("homework", "v1", homework.homework.id.toString())
            }.build()) {
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkUpdateSubjectInstanceRequest(subjectInstanceId = subjectInstanceVppId, groupId = groupVppId))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateSubjectInstanceAndGroup(homework.homework.id, oldSubjectInstanceVppId, oldGroupVppId)
        }
    }

    override suspend fun editHomeworkDueTo(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile) {
        val oldDueTo = homework.dueTo
        vppDatabase.homeworkDao.updateDueTo(homework.id, dueTo)

        if (homework.id < 0 || profile.vppId == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateDueTo(homework.id, oldDueTo) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("homework", "v1", homework.id.toString())
            }.build()) {
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkUpdateDueToRequest(dueTo = dueTo.toString()))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateDueTo(homework.id, oldDueTo)
        }
    }

    override suspend fun editHomeworkVisibility(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile) {
        val oldVisibility = homework.isPublic
        vppDatabase.homeworkDao.updateVisibility(homework.id, isPublic)

        if (homework.id < 0 || profile.vppId == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateVisibility(homework.id, oldVisibility) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("homework", "v1", homework.id.toString())
            }.build()) {
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkUpdateVisibilityRequest(isPublic = isPublic))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateVisibility(homework.id, oldVisibility)
        }
    }

    override suspend fun addTask(homework: Homework, task: String, profile: Profile.StudentProfile): Response.Error? {
        if (homework.id < 0 || profile.vppId == null) {
            val id = getIdForNewLocalHomeworkTask() - 1
            vppDatabase.homeworkDao.upsertTaskMany(
                listOf(
                    DbHomeworkTask(
                        content = task,
                        homeworkId = homework.id,
                        id = id,
                        cachedAt = kotlin.time.Clock.System.now()
                    )
                )
            )
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1", homework.id.toString(), "task")
                }.build())
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkAddTaskRequest(task = task))
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            val id = ResponseDataWrapper.fromJson<Int>(response.bodyAsText()) ?: return Response.Error.ParsingError(response.bodyAsText())
            vppDatabase.homeworkDao.upsertTaskMany(
                listOf(
                    DbHomeworkTask(
                        content = task,
                        homeworkId = homework.id,
                        id = id,
                        cachedAt = kotlin.time.Clock.System.now()
                    )
                )
            )
            return null
        }
        return Response.Error.Cancelled
    }

    override suspend fun editHomeworkTask(task: Homework.HomeworkTask, newContent: String, profile: Profile.StudentProfile) {
        val oldContent = task.content
        vppDatabase.homeworkDao.updateTaskContent(task.id, newContent)

        if (task.id < 0 || profile.vppId == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateTaskContent(task.id, oldContent) }) {
            val response = httpClient.patch {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1", task.homeworkId.toString(), "task", task.id.toString())
                }.build())
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkTaskUpdateContentRequest(content = newContent))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateTaskContent(task.id, oldContent)
        }
    }

    override suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error? {
        if (homework.id < 0 || profile.vppId == null) {
            vppDatabase.homeworkDao.deleteById(listOf(homework.id))
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete(URLBuilder(currentConfiguration.appApiUrl).apply {
                appendPathSegments("homework", "v1", homework.id.toString())
            }.build()) {
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            vppDatabase.homeworkDao.deleteById(listOf(homework.id))
            return null
        }
        return Response.Error.Cancelled
    }

    override suspend fun deleteHomeworkTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error? {
        if (task.id < 0 || profile.vppId == null) {
            vppDatabase.homeworkDao.deleteTaskById(listOf(task.id))
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1", task.homeworkId.toString(), "task", task.id.toString())
                }.build())
                profile.vppId!!.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            vppDatabase.homeworkDao.deleteTaskById(listOf(task.id))
            return null
        }
        return Response.Error.Cancelled
    }

    override suspend fun download(
        schoolApiAccess: VppSchoolAuthentication,
        groups: List<Alias>,
        subjectInstanceAliases: List<Alias>
    ): Response<List<HomeworkDto>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1")
                    parameters.append("filter_groups", groups.joinToString(","))
                    parameters.append("filter_subject_instances", subjectInstanceAliases.joinToString(","))
                    parameters.append("include_tasks", "true")
                    parameters.append("include_files", "true")
                    parameters.append("include_subject_instances", "true")
                }.build())
                schoolApiAccess.authentication(this)
            }

            if (response.status != HttpStatusCode.OK) return response.toErrorResponse()

            val data = response.body<ResponseDataWrapper<List<HomeworkDto>>>().data

            return Response.Success(data)
        }
        return Response.Error.Cancelled
    }

    private val runningDownloads = ConcurrentMutableMap<Int, Deferred<Response.Error?>>()
    suspend fun downloadById(id: Int): Response.Error? {
        runningDownloads[id]?.let { return it.await() }

        val deferred = CoroutineScope(Dispatchers.IO).async download@{
            try {
                val authenticationOptions = getAuthenticationOptionsForRestrictedEntity(httpClient, URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1", id.toString())
                }.buildString())
                if (authenticationOptions !is Response.Success) return@download authenticationOptions as Response.Error

                val authentication =
                    genericAuthenticationProvider.getAuthentication(authenticationOptions.data)
                        ?: return@download Response.Error.Other("No authentication found for school with id ${authenticationOptions.data}")

                val response = httpClient.get(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1", id.toString())
                    parameters.append("include_tasks", "true")
                    parameters.append("include_files", "true")
                }.build()) {
                    authentication.authentication(this)
                }

                if (response.status != HttpStatusCode.OK) {
                    logger.e { "Error downloading homework with id $id: $response" }
                    return@download response.toErrorResponse()
                }

                return@download Response.Error.Other("This is paused")
            } finally {
                runningDownloads.remove(id)
            }
        }
        runningDownloads[id] = deferred
        return deferred.await()
    }

    override suspend fun clearCache() {
        vppDatabase.homeworkDao.deleteCache()
    }

    override suspend fun createHomeworkOnline(
        vppId: VppId.Active,
        until: LocalDate,
        groupId: Int?,
        subjectInstanceId: Int?,
        isPublic: Boolean,
        tasks: List<String>
    ): Response<CreateHomeworkResponse> {
        require(groupId != null || subjectInstanceId != null) { "Either groupId or subjectInstanceId must not be null" }
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1")
                }.build())
                bearerAuth(vppId.accessToken)
                contentType(ContentType.Application.Json)
                setBody(
                    HomeworkPostRequest(
                        subjectInstance = subjectInstanceId,
                        groupId = groupId,
                        dueTo = until.toString(),
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
        return Response.Error.Cancelled
    }

    override suspend fun linkHomeworkFile(vppId: VppId.Active?, homeworkId: Int, fileId: Int): Response<Unit> {
        if (homeworkId > 0) {
            require(vppId != null) { "A vpp.ID must be provided when attaching a file to a cloud homework." }

            safeRequest(onError = { return it }) {
                val response = httpClient.post(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework",  "v1", homeworkId.toString(), "file")
                }.buildString()) {
                    vppId.buildVppSchoolAuthentication().authentication(this)
                    contentType(ContentType.Application.Json)
                    setBody(HomeworkFileLinkRequest(fileId))
                }

                if (!response.status.isSuccess()) return response.toErrorResponse()
            }
        }

        vppDatabase.homeworkDao.upsertHomeworkFileConnection(FKHomeworkFile(homeworkId, fileId))
        return Unit.asSuccess()
    }

    override suspend fun unlinkHomeworkFile(vppId: VppId.Active?, homeworkId: Int, fileId: Int): Response<Unit> {
        if (homeworkId > 0) {
            require(vppId != null) { "A vpp.ID must be provided when detaching a file from a cloud homework." }

            safeRequest(onError = { return it }) {
                val response = httpClient.delete(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1", homeworkId.toString(), "file")
                }.buildString()) {
                    vppId.buildVppSchoolAuthentication().authentication(this)

                    contentType(ContentType.Application.Json)
                    setBody(HomeworkFileLinkRequest(fileId))
                }

                if (!response.status.isSuccess()) return response.toErrorResponse()
            }
        }

        vppDatabase.homeworkDao.deleteFileHomeworkConnection(homeworkId, fileId)
        return Unit.asSuccess()
    }

    override suspend fun dropIndexForProfile(profileId: Uuid) {
        vppDatabase.homeworkDao.dropHomeworkIndexForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, homeworkIds: Collection<Int>) {
        vppDatabase.homeworkDao.upsertHomeworkIndex(homeworkIds.map { DbProfileHomeworkIndex(it, profileId) })
    }
}

@Serializable
data class HomeworkPostRequest(
    @SerialName("subject_instance") val subjectInstance: Int? = null,
    @SerialName("group") val groupId: Int? = null,
    @SerialName("due_to") val dueTo: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("tasks") val tasks: List<String>,
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
data class HomeworkDto(
    @SerialName("id") val id: Int,
    @SerialName("created_by") val createdBy: EntityId,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("due_to") val dueTo: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("group") val group: EntityId?,
    @SerialName("subject_instance") val subjectInstance: EntityId?,
    @SerialName("tasks") val tasks: List<HomeworkTaskDtoWrapper>,
    @SerialName("files") val files: List<EntityId>,
)

@Serializable
data class EntityId(
    @SerialName("id") val id: Int
)

@Serializable
data class HomeworkTaskDtoWrapper(
    @SerialName("value") val value: HomeworkTaskDto
)

@Serializable
data class HomeworkTaskDto(
    @SerialName("id") val id: Int,
    @SerialName("content") val content: String,
    @SerialName("done") val done: Boolean?
)

@Serializable
data class HomeworkTaskUpdateDoneStateRequest(
    @SerialName("is_done") val isDone: Boolean
)

@Serializable
data class HomeworkUpdateSubjectInstanceRequest(
    @SerialName("subject_instance_id") val subjectInstanceId: Int?,
    @SerialName("group_id") val groupId: Int?,
)

@Serializable
data class HomeworkUpdateDueToRequest(
    @SerialName("due_to") val dueTo: String,
)

@Serializable
data class HomeworkUpdateVisibilityRequest(
    @SerialName("is_public") val isPublic: Boolean,
)

@Serializable
data class HomeworkAddTaskRequest(
    @SerialName("task") val task: String,
)

@Serializable
data class HomeworkTaskUpdateContentRequest(
    @SerialName("content") val content: String,
)

@Serializable
data class HomeworkFileLinkRequest(
    @SerialName("file_id") val fileId: Int
)

