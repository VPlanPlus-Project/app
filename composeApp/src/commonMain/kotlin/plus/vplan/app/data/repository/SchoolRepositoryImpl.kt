package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.appApi
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSchool
import plus.vplan.app.data.source.database.model.database.DbSchoolAlias
import plus.vplan.app.data.source.database.model.database.DbSchoolIndiwareAccess
import plus.vplan.app.data.source.network.model.ApiAlias
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.domain.cache.CreationReason
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.SchoolDbDto
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.VppSchoolDto
import kotlin.uuid.Uuid

class SchoolRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val httpClient: HttpClient
) : SchoolRepository {
    override suspend fun upsert(item: SchoolDbDto): Uuid {
        val resolvedId = resolveAliasesToLocalId(item.aliases)
        val schoolId = resolvedId ?: Uuid.random()
        val existing = resolvedId?.let { vppDatabase.schoolDao.findById(it) }?.first()
        vppDatabase.schoolDao.upsertSchool(
            school = DbSchool(
                id = schoolId,
                name = item.name,
                cachedAt = Clock.System.now(),
                creationReason = if (existing?.school?.creationReason == CreationReason.Persisted) CreationReason.Persisted else item.creationReason
            ),
            aliases = item.aliases.map {
                DbSchoolAlias.fromAlias(it, schoolId)
            }
        )
        return schoolId
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.schoolDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }

    override fun getByLocalId(id: Uuid): Flow<School?> {
        return vppDatabase.schoolDao.findById(id).map { embeddedSchool ->
            embeddedSchool?.toModel()
        }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.schoolDao.getAll().map { it.map { school -> school.school.id } }
    }

    override suspend fun downloadSchools(): Response<List<VppSchoolDto>> {
        safeRequest(onError = { return it }) {
            val response = httpClient.get {
                url(URLBuilder(appApi).apply {
                    appendPathSegments("school", "v1", "list")
                }.build())
            }

            val items = response.body<ResponseDataWrapper<List<SchoolItemResponse>>>()

            return Response.Success(items.data.map { schoolItemResponse ->
                VppSchoolDto(
                    id = schoolItemResponse.id,
                    name = schoolItemResponse.name,
                    aliases = schoolItemResponse.buildAliases()
                )
            })
        }
        return Response.Error.Cancelled
    }

    override suspend fun downloadById(identifier: String): Response<VppSchoolDto> {
        safeRequest(onError = { return  it }) {
            val response = httpClient.get {
                url(URLBuilder(appApi).apply {
                    appendPathSegments("school", "v1", "by-id")
                    appendEncodedPathSegments(identifier)
                }.build())
            }

            val items = response.body<ResponseDataWrapper<SchoolItemResponse>>()

            return Response.Success(items.data.let { schoolItemResponse ->
                VppSchoolDto(
                    id = schoolItemResponse.id,
                    name = schoolItemResponse.name,
                    aliases = schoolItemResponse.buildAliases()
                )
            })
        }
        return Response.Error.Cancelled
    }

    override suspend fun setSp24Access(
        schoolId: Uuid,
        sp24Id: Int,
        username: String,
        password: String,
        daysPerWeek: Int,
    ) {
        vppDatabase.schoolDao.upsertSp24SchoolDetails(
            DbSchoolIndiwareAccess(
                schoolId = schoolId,
                sp24SchoolId = sp24Id.toString(),
                username = username,
                password = password,
                daysPerWeek = daysPerWeek,
                credentialsValid = true
            )
        )
    }

    override suspend fun setSp24CredentialValidity(schoolId: Uuid, valid: Boolean) {
        vppDatabase.schoolDao.setIndiwareAccessValidState(schoolId, valid)
    }

    override suspend fun deleteSchool(schoolId: Uuid) {
        vppDatabase.schoolDao.deleteById(schoolId)
    }
}

@Serializable
data class SchoolItemResponse(
    @SerialName("school_id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("address") val address: String?,
    @SerialName("aliases") val aliases: List<ApiAlias>,
    @SerialName("coordinates") val coordinates: String?,
) {
    fun buildAliases(): List<Alias> = aliases.map { it.toModel() }
}