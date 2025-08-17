package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.encodeURLPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.currentConfiguration
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.data.source.network.model.ApiAlias
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppSchoolAuthentication
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.VppSubjectInstanceDto
import kotlin.uuid.Uuid

class SubjectInstanceRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : SubjectInstanceRepository {

    override fun getByGroup(groupId: Uuid): Flow<List<SubjectInstance>> {
        return vppDatabase.subjectInstanceDao.getByGroup(groupId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.subjectInstanceDao.getAll().map { it.map { subjectInstance -> subjectInstance.subjectInstance.id } }
    }

    override fun getBySchool(schoolId: Uuid): Flow<List<SubjectInstance>> {
        return vppDatabase.subjectInstanceDao.getBySchool(schoolId).map { it.map { dl -> dl.toModel() } }
    }

    override suspend fun deleteById(id: Uuid) {
        deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<Uuid>) {
        vppDatabase.subjectInstanceDao.deleteById(ids)
    }

    override fun getByLocalId(id: Uuid): Flow<SubjectInstance?> {
        return vppDatabase.subjectInstanceDao.findById(id).map { it?.toModel() }
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.subjectInstanceDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }

    override suspend fun upsert(item: SubjectInstanceDbDto): Uuid {
        val subjectInstanceId = resolveAliasesToLocalId(item.aliases) ?: Uuid.random()
        vppDatabase.subjectInstanceDao.upsertSubjectInstance(
            entity = DbSubjectInstance(
                id = subjectInstanceId,
                subject = item.subject,
                cachedAt = Clock.System.now(),
                teacherId = item.teacher,
                courseId = item.course
            ),
            aliases = item.aliases.map {
                DbSubjectInstanceAlias.fromAlias(it, subjectInstanceId)
            },
            groups = item.groups.map { FKSubjectInstanceGroup(subjectInstanceId, it) }
        )
        return subjectInstanceId
    }

    override suspend fun downloadSchoolIdById(identifier: String): Response<Int> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("subject-instance", "v1", identifier.encodeURLPath(encodeSlash = true, encodeEncoded = true))
                }.build())
            }

            val data = response.body< ResponseDataWrapper<UnauthenticatedSubjectInstanceResponse>>().data
            return Response.Success(data.schoolId)
        }
        return Response.Error.Cancelled
    }

    override suspend fun downloadById(schoolAuthentication: VppSchoolAuthentication, identifier: String): Response<VppSubjectInstanceDto> {
        safeRequest(onError = { return  it }) {
            val response = httpClient.get {
                url(URLBuilder(currentConfiguration.appApiUrl).apply {
                    appendPathSegments("subject-instance", "v1", identifier.encodeURLPath(encodeSlash = true, encodeEncoded = true))
                }.build())

                schoolAuthentication.authentication(this)
            }

            val item = response.body<ResponseDataWrapper<SubjectInstanceVppItemResponse>>().data

            return Response.Success(VppSubjectInstanceDto(
                id = item.id,
                aliases = item.buildAliases()
            ))
        }
        return Response.Error.Cancelled
    }
}

@Serializable
data class SubjectInstanceVppItemResponse(
    @SerialName("id") val id: Int,
    @SerialName("aliases") val aliases: List<ApiAlias>
) {
    fun buildAliases(): List<Alias> = aliases.map { it.toModel() }
}

@Serializable
data class UnauthenticatedSubjectInstanceResponse(
    @SerialName("school_id") val schoolId: Int,
)