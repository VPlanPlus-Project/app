package plus.vplan.app.data.repository.schulverwalter

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterGrade
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.DbSchulverwalterTeacher
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterInterval
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterCollectionSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterCollection
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterSubject
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSchulverwalterGradeSchulverwalterTeacher
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.repository.schulverwalter.GradeRepository
import plus.vplan.app.utils.sendAll

class GradeRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : GradeRepository {
    override suspend fun download(): Response<Set<Int>> {
        safeRequest(onError = { return it }) {
            val accessTokens = vppDatabase.vppIdDao.getSchulverwalterAccess().first().filter { it.isValid != false }
            val ids = mutableSetOf<Int>()
            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "grades")
                        parameters.append("include", "collection")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@forEach
                val data = ResponseDataWrapper.fromJson<List<GradeItemResponse>>(response.bodyAsText())
                    ?: return Response.Error.ParsingError(response.bodyAsText())

                handleResponse(data, accessToken.schulverwalterUserId, accessToken.vppId)
                ids.addAll(data.map { it.id })
            }
            return Response.Success(ids)
        }
        return Response.Error.Cancelled
    }

    override fun getById(id: Int, forceReload: Boolean): Flow<CacheState<Grade>> = channelFlow {
        val gradeFlow = vppDatabase.gradeDao.getById(id).map { it?.toModel() }
        if (!forceReload) {
            var hadData = false
            sendAll(gradeFlow.takeWhile { it != null }.filterNotNull().onEach { hadData = true }.map { CacheState.Done(it) })
            if (hadData) return@channelFlow
        }
        send(CacheState.Loading(id.toString()))

        safeRequest(onError = { trySend(CacheState.Error(id, it)) }) {
            val existing = vppDatabase.gradeDao.getById(id).first()
            val accessTokens = existing?.let { listOfNotNull(vppDatabase.vppIdDao.getSchulverwalterAccessBySchulverwalterUserId(it.grade.userForRequest).first()) }
                ?: vppDatabase.vppIdDao.getSchulverwalterAccess().first()

            accessTokens.forEach { accessToken ->
                val response = httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "beste.schule"
                        port = 443
                        pathSegments = listOf("api", "grades")
                        parameters.append("include", "collection")
                    }
                    bearerAuth(accessToken.schulverwalterAccessToken)
                }
                if (!response.status.isSuccess()) return@channelFlow send(CacheState.Error(id.toString(), response.toErrorResponse()))
                val data = ResponseDataWrapper.fromJson<List<GradeItemResponse>>(response.bodyAsText())
                    ?: return@channelFlow send(CacheState.Error(id.toString(), Response.Error.ParsingError(response.bodyAsText())))

                handleResponse(data, accessToken.schulverwalterUserId, accessToken.vppId)
            }

            if (gradeFlow.first() == null) return@channelFlow send(CacheState.NotExisting(id.toString()))
            return@channelFlow sendAll(getById(id, false))
        }
    }

    override fun getAllIds(): Flow<List<Int>> = vppDatabase.gradeDao.getAll()

    override suspend fun setConsiderForFinalGrade(gradeId: Int, useForFinalGrade: Boolean) {
        vppDatabase.gradeDao.setConsiderForFinalGrade(gradeId, useForFinalGrade)
    }

    override suspend fun deleteByVppId(vppId: Int) {
        vppDatabase.gradeDao.deleteByVppId(vppId)
    }

    private suspend fun handleResponse(data: List<GradeItemResponse>, userForRequest: Int, vppId: Int) {
        vppDatabase.subjectDao.upsert(
            subjects = data.map { grade ->
                DbSchulverwalterSubject(
                    id = grade.subject.id,
                    name = grade.subject.name,
                    localId = grade.subject.localId,
                    userForRequest = userForRequest,
                    cachedAt = Clock.System.now()
                )
            }
        )

        vppDatabase.schulverwalterTeacherDao.upsert(
            teachers = data.map { grade ->
                DbSchulverwalterTeacher(
                    id = grade.teacher.id,
                    forename = grade.teacher.forename,
                    surname = grade.teacher.surname,
                    localId = grade.teacher.localId,
                    userForRequest = userForRequest,
                    cachedAt = Clock.System.now()
                )
            }
        )

        vppDatabase.collectionDao.upsert(
            collections = data.map { grade ->
                DbSchulverwalterCollection(
                    id = grade.collection.id,
                    type = grade.collection.type,
                    name = grade.collection.name,
                    userForRequest = userForRequest,
                    givenAt = LocalDate.parse(grade.collection.givenAt),
                    cachedAt = Clock.System.now()
                )
            },
            intervalsCrossovers = data.map { grade ->
                FKSchulverwalterCollectionSchulverwalterInterval(
                    collectionId = grade.collection.id,
                    intervalId = grade.collection.intervalId
                )
            },
            subjectsCrossovers = data.map { grade ->
                FKSchulverwalterCollectionSchulverwalterSubject(
                    collectionId = grade.collection.id,
                    subjectId = grade.subject.id
                )
            }
        )

        data.forEach { grade ->
            vppDatabase.collectionDao.deleteSchulverwalterCollectionSchulverwalterInterval(collectionId = grade.collection.id, intervalIds = listOf(grade.collection.intervalId))
            vppDatabase.collectionDao.deleteSchulverwalterCollectionSchulverwalterSubject(collectionId = grade.collection.id, subjectIds = listOf(grade.subject.id))
        }

        val existingGrades = vppDatabase.gradeDao.getAllRaw().first()

        vppDatabase.gradeDao.upsert(
            grades = data.map { grade ->
                val regexForGradeInParentheses = "\\((.*?)\\)".toRegex()
                val matchResult = regexForGradeInParentheses.find(grade.value)

                val isOptional = matchResult != null
                val value =
                    if (matchResult != null) matchResult.groupValues[1]
                    else if (grade.value == "-") null
                    else grade.value

                if (matchResult != null) matchResult.groupValues[1] else grade.value
                DbSchulverwalterGrade(
                    id = grade.id,
                    value = value,
                    isOptional = isOptional,
                    isSelectedForFinalGrade = existingGrades.find { it.id == grade.id }?.isSelectedForFinalGrade ?: true,
                    userForRequest = userForRequest,
                    givenAt = LocalDate.parse(grade.givenAt),
                    vppId = vppId,
                    cachedAt = Clock.System.now()
                )
            },
            collectionsCrossovers = data.map { grade ->
                FKSchulverwalterGradeSchulverwalterCollection(
                    gradeId = grade.id,
                    collectionId = grade.collection.id
                )
            },
            subjectsCrossovers = data.map { grade ->
                FKSchulverwalterGradeSchulverwalterSubject(
                    gradeId = grade.id,
                    subjectId = grade.subject.id
                )
            },
            teachersCrossovers = data.map { grade ->
                FKSchulverwalterGradeSchulverwalterTeacher(
                    gradeId = grade.id,
                    teacherId = grade.teacher.id
                )
            }
        )

        data.forEach { grade ->
            vppDatabase.gradeDao.deleteSchulverwalterGradeSchulverwalterCollection(gradeId = grade.id, collectionIds = listOf(grade.collection.id))
            vppDatabase.gradeDao.deleteSchulverwalterGradeSchulverwalterSubject(gradeId = grade.id, subjectIds = listOf(grade.subject.id))
            vppDatabase.gradeDao.deleteSchulverwalterGradeSchulverwalterTeacher(gradeId = grade.id, teacherIds = listOf(grade.teacher.id))
        }
    }
}

@Serializable
private data class GradeItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("value") val value: String,
    @SerialName("given_at") val givenAt: String,
    @SerialName("subject") val subject: Subject,
    @SerialName("collection") val collection: Collection,
    @SerialName("teacher") val teacher: Teacher,
) {
    @Serializable
    data class Subject(
        @SerialName("id") val id: Int,
        @SerialName("local_id") val localId: String,
        @SerialName("name") val name: String,
    )

    @Serializable
    data class Collection(
        @SerialName("id") val id: Int,
        @SerialName("type") val type: String,
        @SerialName("name") val name: String,
        @SerialName("given_at") val givenAt: String,
        @SerialName("subject_id") val subjectId: Int,
        @SerialName("interval_id") val intervalId: Int,
    )

    @Serializable
    data class Teacher(
        @SerialName("id") val id: Int,
        @SerialName("forename") val forename: String,
        @SerialName("name") val surname: String,
        @SerialName("local_id") val localId: String,
    )
}