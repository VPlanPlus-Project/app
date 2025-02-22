package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbDefaultLessonGroupCrossover
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SchoolApiAccess
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.utils.sendAll

class DefaultLessonRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : DefaultLessonRepository {

    private val notExisting = mutableListOf<String>()

    override fun getByGroup(groupId: Int): Flow<List<DefaultLesson>> {
        return vppDatabase.defaultLessonDao.getByGroup(groupId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getBySchool(schoolId: Int, forceReload: Boolean): Flow<List<DefaultLesson>> {
        if (forceReload) return channelFlow {
            val school = vppDatabase.schoolDao.findById(schoolId).first()?.toModel() ?: return@channelFlow
            val response = httpClient.get {
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "subject", "instance")
                    parameter("include_subject", "true")
                }
                school.getSchoolApiAccess()?.authentication(this) ?: return@channelFlow
            }
            if (!response.status.isSuccess()) return@channelFlow
            val data = ResponseDataWrapper.fromJson<List<SubjectInstanceResponseWithSubject>>(response.bodyAsText()) ?: return@channelFlow
            vppDatabase.defaultLessonDao.upsert(
                defaultLessons = data.map {
                    DbDefaultLesson(
                        id = it.subjectInstanceId,
                        indiwareId = if (school is School.IndiwareSchool && it.sp24Id != null) "sp24.${school.sp24Id}.${it.sp24Id}" else null,
                        subject = it.subject.subject.name,
                        teacherId = it.teacher?.id,
                        courseId = it.course?.id
                    )
                },
                defaultLessonGroupCrossovers = data.flatMap {
                    it.groups.map { groupId ->
                        DbDefaultLessonGroupCrossover(defaultLessonId = it.subjectInstanceId, groupId = groupId.id)
                    }
                }
            )
            sendAll(getBySchool(schoolId, false))
        }
        return vppDatabase.defaultLessonDao.getBySchool(schoolId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getById(id: Int): Flow<CacheState<DefaultLesson>> {
        return vppDatabase.defaultLessonDao.getById(id)
            .map {
                it?.toModel()?.let { model -> CacheState.Done(model) } ?: CacheState.NotExisting(id.toString())
            }
    }

    override fun getByIndiwareId(indiwareId: String): Flow<CacheState<DefaultLesson>> {
        if (indiwareId in notExisting) return flowOf(CacheState.NotExisting(indiwareId))
        return channelFlow {
            var hadData = false
            vppDatabase.defaultLessonDao
                .getByIndiwareId(indiwareId)
                .takeWhile { it != null }
                .filterNotNull()
                .map { CacheState.Done(it.toModel()) }
                .onEach { hadData = true; send(it) }
                .collect()

            if (hadData) return@channelFlow
            send(CacheState.Loading(indiwareId))
            safeRequest(onError = { send(CacheState.Error(indiwareId, it)) }) {
                val school = vppDatabase.schoolDao.getAll().first().firstOrNull { it.sp24SchoolDetails != null && it.sp24SchoolDetails.sp24SchoolId == indiwareId.split(".")[1] }?.toModel()
                if (school?.getSchoolApiAccess() == null) return@channelFlow send(CacheState.Error(indiwareId, Response.Error.Other("no school for course $indiwareId")))
                val schoolResponse = httpClient.get {
                    url {
                        protocol = api.protocol
                        host = api.host
                        port = api.port
                        pathSegments = listOf("api", "v2.2", "subject", "instance", indiwareId)
                        parameter("include_subject", "true")
                    }
                    school.getSchoolApiAccess()!!.authentication(this)
                }
                if (!schoolResponse.status.isSuccess()) return@channelFlow send(CacheState.Error(indiwareId, schoolResponse.toErrorResponse<Any>()))
                val data = ResponseDataWrapper.fromJson<SubjectInstanceResponseWithSubject>(schoolResponse.bodyAsText()) ?: return@channelFlow send(CacheState.Error(indiwareId, Response.Error.ParsingError(schoolResponse.bodyAsText())))
                vppDatabase.defaultLessonDao.upsert(
                    listOf(
                        DbDefaultLesson(
                            id = data.subjectInstanceId,
                            indiwareId = if (school is School.IndiwareSchool && data.sp24Id != null) "sp24.${school.sp24Id}.${data.sp24Id}" else null,
                            subject = data.subject.subject.name,
                            teacherId = data.teacher?.id,
                            courseId = data.course?.id
                        )
                    ),
                    defaultLessonGroupCrossovers = data.groups.map { groupId ->
                        DbDefaultLessonGroupCrossover(data.subjectInstanceId, groupId.id)
                    }
                )
                sendAll(getByIndiwareId(indiwareId))
            }
        }
    }

    override suspend fun download(schoolId: Int, schoolApiAccess: SchoolApiAccess): Response<List<DefaultLesson>> {
        val school = vppDatabase.schoolDao.findById(schoolId).first()?.toModel()
        safeRequest(onError = { return it }) {
            val schoolResponse = httpClient.get {
                url {
                    protocol = api.protocol
                    host = api.host
                    port = api.port
                    pathSegments = listOf("api", "v2.2", "subject", "instance")
                    parameter("include_subject", "true")
                }
                schoolApiAccess.authentication(this)
            }
            if (!schoolResponse.status.isSuccess()) return schoolResponse.toErrorResponse<List<DefaultLesson>>()
            val data = ResponseDataWrapper.fromJson<List<SubjectInstanceResponseWithSubject>>(schoolResponse.bodyAsText()) ?: return Response.Error.ParsingError(schoolResponse.bodyAsText())
            vppDatabase.defaultLessonDao.upsert(
                data.map { item ->
                    DbDefaultLesson(
                        id = item.subjectInstanceId,
                        indiwareId = if (school is School.IndiwareSchool && item.sp24Id != null) "sp24.${school.sp24Id}.${item.sp24Id}" else null,
                        subject = item.subject.subject.name,
                        teacherId = item.teacher?.id,
                        courseId = item.course?.id
                    )
                },
                defaultLessonGroupCrossovers = data.flatMap { item ->
                    item.groups.map { DbDefaultLessonGroupCrossover(item.subjectInstanceId, it.id) }
                }
            )
        }
        return Response.Success(vppDatabase.defaultLessonDao.getBySchool(schoolId).first().map { it.toModel() })
    }

    override suspend fun upsert(defaultLesson: DefaultLesson): DefaultLesson {
        upsert(listOf(defaultLesson))
        return getById(defaultLesson.id).filterIsInstance<CacheState.Done<DefaultLesson>>().first().data
    }

    override suspend fun upsert(defaultLessons: List<DefaultLesson>) {
        vppDatabase.defaultLessonDao.upsert(
            defaultLessons = defaultLessons.map { defaultLesson ->
                DbDefaultLesson(
                    id = defaultLesson.id,
                    indiwareId = defaultLesson.indiwareId,
                    subject = defaultLesson.subject,
                    teacherId = defaultLesson.teacher,
                    courseId = defaultLesson.course
                )
            },
            defaultLessonGroupCrossovers = defaultLessons.flatMap { defaultLesson ->
                defaultLesson.groups.map { group ->
                    DbDefaultLessonGroupCrossover(
                        defaultLessonId = defaultLesson.id,
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
        vppDatabase.defaultLessonDao.deleteById(ids)
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