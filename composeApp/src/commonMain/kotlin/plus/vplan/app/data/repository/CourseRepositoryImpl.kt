package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.utils.sendAll

class CourseRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : CourseRepository {
    override fun getByGroup(groupId: Int): Flow<List<Course>> {
        return vppDatabase.courseDao.getByGroup(groupId)
            .map { it.map { course -> course.toModel() } }
    }

    override fun getBySchool(schoolId: Int, forceReload: Boolean): Flow<List<Course>> {
        if (forceReload) {
            return channelFlow {
                val school = vppDatabase.schoolDao.findById(schoolId).first()?.toModel() ?: return@channelFlow
                val response = httpClient.get {
                    url {
                        protocol = api.protocol
                        host = api.host
                        port = api.port
                        pathSegments = listOf("api", "v2.2", "subject", "course")
                        parameter("include_teacher", "true")
                    }
                    school.getSchoolApiAccess()?.authentication(this) ?: return@channelFlow
                }
                if (!response.status.isSuccess()) return@channelFlow
                val data = ResponseDataWrapper.fromJson<List<CourseItemResponse>>(response.bodyAsText()) ?: return@channelFlow
                vppDatabase.courseDao.upsert(
                    courses = data.map {
                        DbCourse(
                            id = it.courseId,
                            indiwareId = if (school is School.IndiwareSchool) "sp24.${school.sp24Id}.${it.name}+${it.teacher?.value?.name ?: ""}" else null,
                            name = it.name,
                            teacherId = it.teacher?.teacher
                        )
                    },
                    courseGroupCrossovers = data.flatMap {
                        it.groups.map { groupId ->
                            DbCourseGroupCrossover(
                                groupId = groupId.group,
                                courseId = it.courseId
                            )
                        }
                    }
                )
                sendAll(getBySchool(schoolId, false))
            }
        }
        return vppDatabase.courseDao.getBySchool(schoolId)
            .map { it.map { course -> course.toModel() } }
    }

    override fun getById(id: Int): Flow<CacheState<Course>> {
        return vppDatabase.courseDao.getById(id).map { it?.toModel()?.let { model -> CacheState.Done(model) } ?: CacheState.NotExisting(id.toString()) }
    }

    override fun getByIndiwareId(indiwareId: String): Flow<CacheState<Course>> {
        return channelFlow {
            var hadData = false
            vppDatabase.courseDao
                .getByIndiwareId(indiwareId)
                .takeWhile { it != null }
                .filterNotNull()
                .onEach { hadData = true }
                .map { CacheState.Done(it.toModel()) }
                .let { sendAll(it) }

            if (hadData) return@channelFlow
            safeRequest(onError = { send(CacheState.Error(indiwareId, it)) }) {
                val school = vppDatabase.schoolDao.getAll().first().firstOrNull { it.sp24SchoolDetails != null && it.sp24SchoolDetails.sp24SchoolId == indiwareId.split(".")[1] }?.toModel()
                if (school?.getSchoolApiAccess() == null) return@channelFlow send(CacheState.Error(indiwareId, Response.Error.Other("no school for course $indiwareId")))
                val response = httpClient.get {
                    url {
                        protocol = api.protocol
                        host = api.host
                        port = api.port
                        pathSegments = listOf("api", "v2.2", "subject", "course", indiwareId)
                    }
                    school.getSchoolApiAccess()!!.authentication(this)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(indiwareId, response.toErrorResponse<Any>()))
                val data = ResponseDataWrapper.fromJson<CourseItemResponse>(response.bodyAsText()) ?: return@channelFlow send(CacheState.Error(indiwareId, Response.Error.ParsingError(response.bodyAsText())))
                vppDatabase.courseDao.upsert(
                    courses = listOf(DbCourse(
                        id = data.courseId,
                        indiwareId = indiwareId,
                        name = data.name,
                        teacherId = data.teacher?.teacher
                    )),
                    courseGroupCrossovers = data.groups.map {
                        DbCourseGroupCrossover(
                            courseId = data.courseId,
                            groupId = it.group
                        )
                    }
                )
                return@channelFlow sendAll(getById(data.courseId))
            }
        }
    }

    override suspend fun upsert(course: Course): Course {
        upsert(listOf(course))
        return getById(course.id).filterIsInstance<CacheState.Done<Course>>().first().data
    }

    override suspend fun upsert(courses: List<Course>) {
        vppDatabase.courseDao.upsert(
            courses = courses.map { course ->
                DbCourse(
                    id = course.id,
                    indiwareId = course.indiwareId,
                    name = course.name,
                    teacherId = course.teacherId
                )
            },
            courseGroupCrossovers = courses.flatMap { course ->
                course.groups.map { group ->
                    DbCourseGroupCrossover(
                        courseId = course.id,
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
        vppDatabase.courseDao.deleteById(ids)
    }
}

@Serializable
private data class CourseItemResponse(
    @SerialName("id") val courseId: Int,
    @SerialName("course_name") val name: String,
    @SerialName("teacher") val teacher: TeacherItemWrapper?,
    @SerialName("groups") val groups: List<GroupItemWrapper>
) {
    @Serializable
    data class TeacherItemWrapper(
        @SerialName("id") val teacher: Int,
        @SerialName("value") val value: TeacherItem
    ) {
        @Serializable
        data class TeacherItem(
            @SerialName("name") val name: String
        )
    }

    @Serializable
    data class GroupItemWrapper(
        @SerialName("id") val group: Int,
    )
}