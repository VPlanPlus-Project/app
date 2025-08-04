package plus.vplan.app.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import plus.vplan.app.api
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbGroup
import plus.vplan.app.data.source.database.model.database.DbGroupAlias
import plus.vplan.app.data.source.network.safeRequest
import plus.vplan.app.data.source.network.toErrorResponse
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import kotlin.uuid.Uuid

class GroupRepositoryImpl(
    private val httpClient: HttpClient,
    private val vppDatabase: VppDatabase
) : GroupRepository {
    override fun getBySchool(schoolId: Uuid): Flow<List<Group>> {
        return vppDatabase.groupDao.getBySchool(schoolId)
            .map { result -> result.map { it.toModel() } }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.groupDao.getAll().map { it.map { group -> group.group.id } }
    }

    override fun getByLocalId(id: Uuid): Flow<Group?> {
        return vppDatabase.groupDao.findById(id).map { embeddedGroup ->
            embeddedGroup?.toModel()
        }
    }

    override suspend fun upsert(item: GroupDbDto): Uuid {
        val groupId = resolveAliasesToLocalId(item.aliases) ?: Uuid.random()
        vppDatabase.groupDao.upsertGroup(
            group = DbGroup(
                id = groupId,
                name = item.name,
                schoolId = item.schoolId,
                cachedAt = Clock.System.now()
            ),
            aliases = item.aliases.map {
                DbGroupAlias.fromAlias(it, groupId)
            }
        )
        return groupId
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.groupDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }

    override suspend fun updateFirebaseToken(group: Group, token: String): Response.Error? {
        safeRequest(onError = { return it }) {
            val response = httpClient.post {
                url(URLBuilder(api).apply {
                    appendPathSegments("api", "v2.2", "group", group.id.toString(), "firebase")
                }.build())

                val school = group.school.getFirstValue() as? School.AppSchool
                school?.buildSp24AppAuthentication()?.authentication(this) ?: return Response.Error.Other("No school api access to update firebase token")
                contentType(ContentType.Application.Json)
                setBody(FirebaseTokenRequest(token))
            }
            if (response.status.isSuccess()) return null
            return response.toErrorResponse<Unit>()
        }
        return Response.Error.Cancelled
    }
}
