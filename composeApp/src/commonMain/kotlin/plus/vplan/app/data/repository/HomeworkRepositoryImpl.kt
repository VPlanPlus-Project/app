@file:OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)

package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbFile
import plus.vplan.app.data.source.database.model.database.DbHomework
import plus.vplan.app.data.source.database.model.database.DbHomeworkTask
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneAccount
import plus.vplan.app.data.source.database.model.database.DbHomeworkTaskDoneProfile
import plus.vplan.app.data.source.database.model.database.DbProfileHomeworkIndex
import plus.vplan.app.data.source.database.model.database.foreign_key.FKHomeworkFile
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.CreateHomeworkResponse
import plus.vplan.app.domain.repository.DownloadHomeworkResponseItem
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.ui.common.AttachedFile
import plus.vplan.app.utils.sha256
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val logger = Logger.withTag("HomeworkRepositoryImpl")

class HomeworkRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase,
) : HomeworkRepository {
    override suspend fun upsert(homework: List<Homework>, tasks: List<Homework.HomeworkTask>, files: List<Homework.HomeworkFile>) {
        vppDatabase.homeworkDao.deleteFileHomeworkConnections(homework.map { it.id })
        vppDatabase.homeworkDao.upsertMany(
            homework = homework.map { homeworkItem ->
                DbHomework(
                    id = homeworkItem.id,
                    subjectInstanceId = homeworkItem.subjectInstanceId,
                    groupId = (homeworkItem as? Homework.LocalHomework)?.getCreatedByProfile()?.groupId ?: homeworkItem.group?.getFirstValue()?.id,
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
                    cachedAt = Clock.System.now()
                )
            },
            homeworkTask = tasks.map { homeworkTask ->
                DbHomeworkTask(
                    id = homeworkTask.id,
                    homeworkId = homeworkTask.homework,
                    content = homeworkTask.content,
                    cachedAt = Clock.System.now()
                )
            },
            homeworkTaskDoneAccount = emptyList(),
            files = files.map { file ->
                DbFile(
                    id = file.id,
                    size = file.size,
                    createdAt = homework.first { file.homework == it.id }.createdAt,
                    createdByVppId = (homework.first { file.homework == it.id } as? Homework.CloudHomework)?.createdBy,
                    fileName = file.name,
                    isOfflineReady = true,
                    cachedAt = Clock.System.now()
                )
            },
            fileHomeworkConnections = files.map {
                FKHomeworkFile(
                    homeworkId = it.homework,
                    fileId = it.id
                )
            }
        )
    }

    override fun getByGroup(groupId: Uuid): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getAll().map { flowData ->
            val subjectInstances = vppDatabase.subjectInstanceDao.getByGroup(groupId).first()
            flowData.filter {
                it.homework.groupId == groupId || subjectInstances.any { subjectInstance -> subjectInstance.groups.any { group -> group.groupId == groupId } }
            }.map { it.toModel() }
        }
    }

    override fun getTaskById(id: Int): Flow<CacheState<Homework.HomeworkTask>> {
        return vppDatabase.homeworkDao.getTaskById(id).map { it?.toModel() }.map { if (it == null) CacheState.NotExisting(id.toString()) else CacheState.Done(it) }
    }

    override fun getAll(): Flow<List<CacheState<Homework>>> {
        return vppDatabase.homeworkDao.getAll().map { it.map { embeddedHomework -> CacheState.Done(embeddedHomework.toModel()) } }
    }

    override fun getByDate(date: LocalDate): Flow<List<Homework>> {
        return vppDatabase.homeworkDao.getByDate(date).map { it.map { it.toModel() } }
    }

    override fun getByProfile(profileId: Uuid, date: LocalDate?): Flow<List<Homework>> {
        if (date == null) return vppDatabase.homeworkDao.getByProfile(profileId).map { it.map { it.toModel() } }
        return vppDatabase.homeworkDao.getByProfileAndDate(profileId, date).map { it.map { it.toModel() } }
    }

    override fun getAllIds(): Flow<List<Int>> {
        return vppDatabase.homeworkDao.getAll().map { it.map { homework -> homework.homework.id } }
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Homework>> {
        return flowOf()
        // TODO
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
        if (profile.getVppIdItem() == null || task.id < 0) {
            vppDatabase.homeworkDao.upsertTaskDoneProfile(DbHomeworkTaskDoneProfile(task.id, profile.id, newState))
            return
        }
        vppDatabase.homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, profile.vppIdId!!, newState))
        safeRequest(onError = { vppDatabase.homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, profile.vppIdId, oldState)) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                appendPathSegments("api", "v2.2", "homework", "task", task.id.toString())
            }.build()) {
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkTaskUpdateDoneStateRequest(isDone = newState))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.upsertTaskDoneAccount(DbHomeworkTaskDoneAccount(task.id, profile.vppIdId, oldState))
        }
    }

    override suspend fun editHomeworkSubjectInstance(homework: Homework, subjectInstance: SubjectInstance?, group: Group?, profile: Profile.StudentProfile) {
        TODO()
        require((subjectInstance == null) xor (group == null)) { "Either subjectInstance or group must not be null" }
        val oldSubjectInstance = homework.subjectInstance?.getFirstValue()
        val oldGroup = homework.group?.getFirstValue()
        vppDatabase.homeworkDao.updateSubjectInstanceAndGroup(homework.id, TODO(), group?.id)

        if (homework.id < 0 || profile.getVppIdItem() == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateSubjectInstanceAndGroup(homework.id, TODO(), oldGroup?.id) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                appendPathSegments("api", "v2.2", "homework", homework.id.toString())
            }.build()) {
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkUpdateSubjectInstanceRequest(subjectInstanceId = TODO(), groupId = -1))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateSubjectInstanceAndGroup(homework.id, TODO(), oldGroup?.id)
        }
    }

    override suspend fun editHomeworkDueTo(homework: Homework, dueTo: LocalDate, profile: Profile.StudentProfile) {
        val oldDueTo = homework.dueTo
        vppDatabase.homeworkDao.updateDueTo(homework.id, dueTo)

        if (homework.id < 0 || profile.getVppIdItem() == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateDueTo(homework.id, oldDueTo) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                appendPathSegments("api", "v2.2", "homework", homework.id.toString())
            }.build()) {
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkUpdateDueToRequest(dueTo = dueTo.toString()))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateDueTo(homework.id, oldDueTo)
        }
    }

    override suspend fun editHomeworkVisibility(homework: Homework.CloudHomework, isPublic: Boolean, profile: Profile.StudentProfile) {
        val oldVisibility = homework.isPublic
        vppDatabase.homeworkDao.updateVisibility(homework.id, isPublic)

        if (homework.id < 0 || profile.getVppIdItem() == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateVisibility(homework.id, oldVisibility) }) {
            val response = httpClient.patch(URLBuilder(currentConfiguration.apiUrl).apply {
                appendPathSegments("api", "v2.2", "homework", homework.id.toString())
            }.build()) {
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkUpdateVisibilityRequest(isPublic = isPublic))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateVisibility(homework.id, oldVisibility)
        }
    }

    override suspend fun addTask(homework: Homework, task: String, profile: Profile.StudentProfile): Response.Error? {
        if (homework.id < 0 || profile.getVppIdItem() == null) {
            val id = getIdForNewLocalHomeworkTask() - 1
            vppDatabase.homeworkDao.upsertTaskMany(
                listOf(
                    DbHomeworkTask(
                        content = task,
                        homeworkId = homework.id,
                        id = id,
                        cachedAt = Clock.System.now()
                    )
                )
            )
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "homework", homework.id.toString(), "tasks")
                }.build())
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
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
                        cachedAt = Clock.System.now()
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

        if (task.id < 0 || profile.getVppIdItem() == null) return
        safeRequest(onError = { vppDatabase.homeworkDao.updateTaskContent(task.id, oldContent) }) {
            val response = httpClient.patch {
                url(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "homework", "task", task.id.toString())
                }.build())
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
                contentType(ContentType.Application.Json)
                setBody(HomeworkTaskUpdateContentRequest(content = newContent))
            }
            if (!response.status.isSuccess()) vppDatabase.homeworkDao.updateTaskContent(task.id, oldContent)
        }
    }

    override suspend fun linkHomeworkFileLocally(homework: Homework, file: plus.vplan.app.domain.model.File) {
        vppDatabase.homeworkDao.upsertFileHomeworkConnections(listOf(FKHomeworkFile(homeworkId = homework.id, fileId = file.id)))
    }

    override suspend fun unlinkHomeworkFileLocally(homework: Homework, fileId: Int) {
        vppDatabase.homeworkDao.deleteFileHomeworkConnections(homework.id, fileId)
    }

    override suspend fun deleteHomework(homework: Homework, profile: Profile.StudentProfile): Response.Error? {
        if (homework.id < 0 || profile.getVppIdItem() == null) {
            vppDatabase.homeworkDao.deleteById(listOf(homework.id))
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete(URLBuilder(currentConfiguration.apiUrl).apply {
                appendPathSegments("api", "v2.2", "homework", homework.id.toString())
            }.build()) {
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            vppDatabase.homeworkDao.deleteById(listOf(homework.id))
            return null
        }
        return Response.Error.Cancelled
    }

    override suspend fun deleteHomeworkTask(task: Homework.HomeworkTask, profile: Profile.StudentProfile): Response.Error? {
        if (task.id < 0 || profile.getVppIdItem() == null) {
            vppDatabase.homeworkDao.deleteTaskById(listOf(task.id))
            return null
        }
        safeRequest(onError = { return it }) {
            val response = httpClient.delete {
                url(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "homework", "task", task.id.toString())
                }.build())
                profile.getVppIdItem()!!.buildVppSchoolAuthentication().authentication(this)
            }
            if (!response.status.isSuccess()) return response.toErrorResponse()
            vppDatabase.homeworkDao.deleteTaskById(listOf(task.id))
            return null
        }
        return Response.Error.Cancelled
    }

    override suspend fun download(schoolApiAccess: VppSchoolAuthentication, groups: List<Alias>, subjectInstanceAliases: List<Alias>): Response<List<DownloadHomeworkResponseItem>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("homework", "v1")
                    parameters.append("filter_groups", groups.joinToString(","))
                    parameters.append("filter_subject_instances", subjectInstanceAliases.joinToString(","))
                    parameters.append("include_tasks", "true")
                    parameters.append("include_files", "true")
                }.build())
                schoolApiAccess.authentication(this)
            }

            if (response.status != HttpStatusCode.OK) return response.toErrorResponse()

            val data = ResponseDataWrapper.fromJson<List<HomeworkGetResponse>>(response.bodyAsText()) ?: return Response.Error.ParsingError(response.bodyAsText())

            return Response.Success(data.map { homework ->
                DownloadHomeworkResponseItem(
                    id = homework.id,
                    subjectInstance = homework.subjectInstance?.id,
                    group = homework.group?.id,
                    createdBy = homework.createdBy.id,
                    dueTo = LocalDate.parse(homework.dueTo),
                    createdAt = kotlin.time.Instant.fromEpochSeconds(homework.createdAt),
                    tasks = homework.tasks.map { task ->
                        DownloadHomeworkResponseItem.Task(
                            id = task.value.id,
                            done = task.value.done,
                            content = task.value.content
                        )
                    },
                    files = homework.files.map { file ->
                        DownloadHomeworkResponseItem.File(
                            id = file.id
                        )
                    }
                )
            })
        }
        return Response.Error.Cancelled
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

    override suspend fun uploadHomeworkDocument(vppId: VppId.Active, homeworkId: Int, document: AttachedFile): Response<Int> {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(currentConfiguration.apiUrl).apply {
                    appendPathSegments("api", "v2.2", "homework", homeworkId.toString(), "documents")
                }.build())
                header("File-Name", document.name)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                header(HttpHeaders.ContentLength, document.size.toString())
                bearerAuth(vppId.accessToken)
                setBody(ByteReadChannel(document.platformFile.readBytes()))
            }
            if (response.status != HttpStatusCode.OK) return response.toErrorResponse()
            return ResponseDataWrapper.fromJson<Int>(response.bodyAsText())?.let { Response.Success(it) } ?: Response.Error.ParsingError(response.bodyAsText())
        }
        return Response.Error.Cancelled
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
private data class HomeworkGetResponse(
    @SerialName("id") val id: Int,
    @SerialName("created_by") val createdBy: EntityId,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("due_to") val dueTo: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("group") val group: EntityId?,
    @SerialName("subject_instance") val subjectInstance: EntityId?,
    @SerialName("tasks") val tasks: List<HomeworkGetResponseTaskItem>,
    @SerialName("files") val files: List<EntityId>,
)

@Serializable
private data class EntityId(
    @SerialName("id") val id: Int
)

@Serializable
private data class HomeworkGetResponseTaskItem(
    @SerialName("value") val value: HomeworkGetResponseTask
)

@Serializable
private data class HomeworkGetResponseTask(
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
