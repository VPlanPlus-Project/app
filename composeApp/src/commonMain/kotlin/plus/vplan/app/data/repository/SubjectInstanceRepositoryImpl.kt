package plus.vplan.app.data.repository

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.network.isResponseFromBackend
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheStateOld
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.utils.sendAll
import kotlin.uuid.Uuid

class SubjectInstanceRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : SubjectInstanceRepository {

    private val notExisting = mutableListOf<String>()

    override fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>> {
        return vppDatabase.subjectInstanceDao.getByGroup(groupId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getAll(): Flow<List<SubjectInstance>> {
        return vppDatabase.subjectInstanceDao.getAll().map { it.map { embeddedSubjectInstance -> embeddedSubjectInstance.toModel() } }
    }

    override fun getBySchool(schoolId: Uuid, forceReload: Boolean): Flow<List<SubjectInstance>> {
        return flowOf()
        if (forceReload) return channelFlow {
            val school = vppDatabase.schoolDao.findById(schoolId).first()?.toModel() ?: return@channelFlow
            safeRequest(onError = { trySend(emptyList()) }) {
                val response = httpClient.get {
                    url(URLBuilder(api).apply {
                        appendPathSegments("api", "v2.2", "subject", "instance")
                        parameters.append("include_subject", "true")
                    }.build())
                    school.getSchoolApiAccess()?.authentication(this) ?: return@channelFlow
                }
                if (!response.status.isSuccess()) return@channelFlow
                val data = ResponseDataWrapper.fromJson<List<SubjectInstanceResponseWithSubject>>(response.bodyAsText()) ?: return@channelFlow
                vppDatabase.subjectInstanceDao.upsert(
                    subjectInstances = data.map {
                        DbSubjectInstance(
                            id = it.subjectInstanceId,
                            indiwareId = if (school is School.Sp24School && it.sp24Id != null) "sp24.${school.sp24Id}.${it.sp24Id}" else null,
                            subject = it.subject.subject.name,
                            teacherId = Uuid.NIL,
                            courseId = it.course?.id,
                            cachedAt = Clock.System.now()
                        )
                    },
                    subjectInstanceGroupCrossovers = data.flatMap {
                        it.groups.map { groupId ->
                            FKSubjectInstanceGroup(subjectInstanceId = it.subjectInstanceId, groupId = Uuid.NIL)
                        }
                    }
                )
                sendAll(getBySchool(schoolId, false))
            }
        }
        return vppDatabase.subjectInstanceDao.getBySchool(schoolId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheStateOld<SubjectInstance>> {
        return flowOf()
        val subjectInstanceFlow = vppDatabase.subjectInstanceDao.getById(id).map { it?.toModel() }
        return channelFlow {
            if (!forceReload) {
                var hadData = false
                subjectInstanceFlow
                    .takeWhile { it != null }
                    .filterNotNull()
                    .onEach { hadData = true; trySend(CacheStateOld.Done(it)) }
                    .collect()
                if (hadData) return@channelFlow
            }

            send(CacheStateOld.Loading(id.toString()))

            safeRequest(onError = { send(CacheStateOld.Error(id.toString(), it)) }) {
                val existing = vppDatabase.subjectInstanceDao.getById(id).first()
                var schoolApiAccess: SchoolApiAccess? = null
                if (existing != null) {
                    schoolApiAccess = existing.groups
                        .mapNotNull { vppDatabase.groupDao.findById(it.groupId).first() }
                        .firstNotNullOfOrNull { vppDatabase.schoolDao.findById(it.group.schoolId).first()?.toModel()?.getSchoolApiAccess() }
                }
                if (schoolApiAccess == null) {
                    val accessResponse = httpClient.get {
                        url(URLBuilder(api).apply {
                            appendPathSegments("api", "v2.2", "subject", "instance", id.toString())
                            parameters.append("include_subject", "true")
                        }.build())
                    }
                    if (accessResponse.status == HttpStatusCode.NotFound && accessResponse.isResponseFromBackend()) {
                        vppDatabase.subjectInstanceDao.deleteById(listOf(id))
                        return@channelFlow send(CacheStateOld.NotExisting(id.toString()))
                    }

                    if (!accessResponse.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), accessResponse.toErrorResponse<SubjectInstance>()))
                    val accessData = ResponseDataWrapper.fromJson<SubjectInstanceUnauthenticatedResponse>(accessResponse.bodyAsText())
                        ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(accessResponse.bodyAsText())))

                    schoolApiAccess = accessData.schoolIds.mapNotNull {
                        val school = vppDatabase.schoolDao.findById(Uuid.NIL).first()?.toModel() ?: return@mapNotNull null
                        if (school is School.Sp24School && !school.credentialsValid) return@mapNotNull null
                        return@mapNotNull school.getSchoolApiAccess()
                    }.firstOrNull()
                }

                if (schoolApiAccess == null) {
                    Logger.i { "No school to update subjectInstance $id" }
                    vppDatabase.subjectInstanceDao.deleteById(id)
                    trySend(CacheStateOld.NotExisting(id.toString()))
                    return@channelFlow
                }

                val response = httpClient.get {
                    url(URLBuilder(api).apply {
                        appendPathSegments("api", "v2.2", "subject", "instance", id.toString())
                        parameters.append("include_subject", "true")
                    }.build())
                    schoolApiAccess!!.authentication(this)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(id.toString(), response.toErrorResponse<SubjectInstance>()))
                val data = ResponseDataWrapper.fromJson<SubjectInstanceResponseWithSubject>(response.bodyAsText()) ?: return@channelFlow send(CacheStateOld.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))
                vppDatabase.subjectInstanceDao.upsert(
                    listOf(
                        DbSubjectInstance(
                            id = data.subjectInstanceId,
                            indiwareId = if (schoolApiAccess is SchoolApiAccess.IndiwareAccess && data.sp24Id != null) "sp24.${schoolApiAccess.sp24id}.${data.sp24Id}" else null,
                            subject = data.subject.subject.name,
                            teacherId = Uuid.NIL,
                            courseId = data.course?.id,
                            cachedAt = Clock.System.now()
                        )
                    ),
                    subjectInstanceGroupCrossovers = data.groups.map { groupId ->
                        FKSubjectInstanceGroup(data.subjectInstanceId, Uuid.NIL)
                    }
                )

                return@channelFlow sendAll(getById(id, false))
            }
        }
    }

    override fun lookupBySp24Id(indiwareId: String): Flow<CacheStateOld<SubjectInstance>> {
        return flowOf()
        if (!Regex("""^sp24\.\d{8}\.\d+$""").matches(indiwareId)) {
            throw IllegalArgumentException("Invalid Indiware ID format: $indiwareId")
        }
        if (indiwareId in notExisting) return flowOf(CacheStateOld.NotExisting(indiwareId))
        return channelFlow {
            var hadData = false
            vppDatabase.subjectInstanceDao
                .getByIndiwareId(indiwareId)
                .takeWhile { it != null }
                .filterNotNull()
                .map { CacheStateOld.Done(it.toModel()) }
                .onEach { hadData = true; send(it) }
                .collect()

            if (hadData) return@channelFlow
            send(CacheStateOld.Loading(indiwareId))
            safeRequest(onError = { send(CacheStateOld.Error(indiwareId, it)) }) {
                val school = vppDatabase.schoolDao.getAll().first().firstOrNull { it.sp24SchoolDetails != null && it.sp24SchoolDetails.sp24SchoolId == indiwareId.split(".")[1] }?.toModel()
                if (school?.getSchoolApiAccess() == null) return@channelFlow send(CacheStateOld.Error(indiwareId, Response.Error.Other("no school for subject instance $indiwareId")))
                val schoolResponse = httpClient.get {
                    url(URLBuilder(api).apply {
                        appendPathSegments("api", "v2.2", "subject", "instance", indiwareId)
                        parameters.append("include_subject", "true")
                    }.build())

                    school!!.getSchoolApiAccess()!!.authentication(this)
                }
                if (!schoolResponse.status.isSuccess()) return@channelFlow send(CacheStateOld.Error(indiwareId, schoolResponse.toErrorResponse<Any>()))
                val data = ResponseDataWrapper.fromJson<SubjectInstanceResponseWithSubject>(schoolResponse.bodyAsText()) ?: return@channelFlow send(CacheStateOld.Error(indiwareId, Response.Error.ParsingError(schoolResponse.bodyAsText())))
                vppDatabase.subjectInstanceDao.upsert(
                    listOf(
                        DbSubjectInstance(
                            id = data.subjectInstanceId,
                            indiwareId = if (school is School.Sp24School && data.sp24Id != null) "sp24.${school.sp24Id}.${data.sp24Id}" else null,
                            subject = data.subject.subject.name,
                            teacherId = Uuid.NIL,
                            courseId = data.course?.id,
                            cachedAt = Clock.System.now()
                        )
                    ),
                    subjectInstanceGroupCrossovers = data.groups.map { groupId ->
                        FKSubjectInstanceGroup(data.subjectInstanceId, Uuid.NIL)
                    }
                )
                sendAll(lookupBySp24Id(indiwareId))
            }
        }
    }

    override suspend fun download(schoolId: Uuid, schoolApiAccess: SchoolApiAccess): Response<List<SubjectInstance>> {
        return Response.Error.Cancelled
        val school = vppDatabase.schoolDao.findById(schoolId).first()?.toModel()
        safeRequest(onError = { return it }) {
            val schoolResponse = httpClient.get {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "subject", "instance")
                    parameters.append("include_subject", "true")
                }.build())
                schoolApiAccess.authentication(this)
            }
            if (!schoolResponse.status.isSuccess()) return schoolResponse.toErrorResponse<List<SubjectInstance>>()
            val data = ResponseDataWrapper.fromJson<List<SubjectInstanceResponseWithSubject>>(schoolResponse.bodyAsText()) ?: return Response.Error.ParsingError(schoolResponse.bodyAsText())
            vppDatabase.subjectInstanceDao.upsert(
                data.map { item ->
                    DbSubjectInstance(
                        id = item.subjectInstanceId,
                        indiwareId = if (school is School.Sp24School && item.sp24Id != null) "sp24.${school.sp24Id}.${item.sp24Id}" else null,
                        subject = item.subject.subject.name,
                        teacherId = Uuid.NIL,
                        courseId = item.course?.id,
                        cachedAt = Clock.System.now()
                    )
                },
                subjectInstanceGroupCrossovers = data.flatMap { item ->
                    item.groups.map { FKSubjectInstanceGroup(item.subjectInstanceId, Uuid.NIL) }
                }
            )
        }
        return Response.Success(vppDatabase.subjectInstanceDao.getBySchool(schoolId).first().map { it.toModel() })
    }

    override suspend fun upsert(subjectInstance: SubjectInstance): SubjectInstance {
        upsert(listOf(subjectInstance))
        return getById(subjectInstance.id, false).filterIsInstance<CacheStateOld.Done<SubjectInstance>>().first().data
    }

    override suspend fun upsert(subjectInstances: List<SubjectInstance>) {
        vppDatabase.subjectInstanceDao.upsert(
            subjectInstances = subjectInstances.map { subjectInstance ->
                DbSubjectInstance(
                    id = subjectInstance.id,
                    indiwareId = subjectInstance.indiwareId,
                    subject = subjectInstance.subject,
                    teacherId = subjectInstance.teacher,
                    courseId = subjectInstance.course,
                    cachedAt = Clock.System.now()
                )
            },
            subjectInstanceGroupCrossovers = subjectInstances.flatMap { subjectInstance ->
                subjectInstance.groups.map { group ->
                    FKSubjectInstanceGroup(
                        subjectInstanceId = subjectInstance.id,
                        groupId = group
                    )
                }
            }
        )
    }

    override suspend fun deleteById(id: Int) {
        deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<Int>) {
        vppDatabase.subjectInstanceDao.deleteById(ids)
    }
}

@Serializable
private data class SubjectInstanceResponseWithSubject(
    @SerialName("school_ids") val schoolIds: List<Int>,
    @SerialName("id") val subjectInstanceId: Int,
    @SerialName("subject") val subject: SubjectResponseWrapper,
    @SerialName("teacher") val teacher: TeacherResponse?,
    @SerialName("course") val course: CourseResponse?,
    @SerialName("groups") val groups: List<GroupResponse>,
    @SerialName("sp24_id") val sp24Id: Int?,
)

@Serializable
private data class SubjectResponseWrapper(
    @SerialName("value") val subject: SubjectResponse
)

@Serializable
private data class SubjectResponse(
    @SerialName("subject") val name: String
)

@Serializable
private data class CourseResponse(
    @SerialName("id") val id: Int
)

@Serializable
private data class TeacherResponse(
    @SerialName("id") val id: Int
)

@Serializable
private data class GroupResponse(
    @SerialName("id") val id: Int
)

@Serializable
private data class SubjectInstanceUnauthenticatedResponse(
    @SerialName("school_ids") val schoolIds: List<Int>
)