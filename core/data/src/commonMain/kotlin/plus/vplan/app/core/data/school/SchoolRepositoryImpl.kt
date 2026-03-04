@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.school

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.SchoolDao
import plus.vplan.app.core.database.model.database.DbSchool
import plus.vplan.app.core.database.model.database.DbSchoolAlias
import plus.vplan.app.core.database.model.database.DbSchoolSp24Acess
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.CreationReason
import plus.vplan.app.core.model.School
import plus.vplan.app.network.vpp.school.SchoolApi
import kotlin.time.Clock
import kotlin.uuid.Uuid

class SchoolRepositoryImpl(
    private val schoolApi: SchoolApi,
    private val schoolDao: SchoolDao,
) : SchoolRepository {

    val schools = mutableListOf<School>()
    override suspend fun getAll(): List<School> {
        if (schools.isNotEmpty()) return schools

        val schoolItems = schoolApi.getAll()

        schools.addAll(schoolItems.map { item ->
            School.CachedSchool(
                id = Uuid.NIL,
                name = item.name,
                aliases = item.aliases.mapNotNull { alias -> alias.toModel() }.toSet(),
                cachedAt = Clock.System.now()
            )
        })

        return schools
    }

    override fun getByIds(identifier: Set<Alias>): Flow<School?> {
        if (identifier.isEmpty()) throw IllegalArgumentException("Identifiers cannot be empty")
        return this
            .findLocalIdByIdentifier(identifier)
            .distinctUntilChanged()
            .flatMapLatest { id -> id?.let { schoolDao.findById(id).map { it?.toModel() }.distinctUntilChanged() } ?: flowOf(null) }
            .map { school ->
                if (school == null) {
                    val result = schoolApi.getByAlias(identifier.first()) ?: return@map null

                    // Check if we already know the school by another alias
                    val resultAliases = result.aliases.mapNotNull { it.toModel() }.toSet()

                    val maybeExistingLocalId = findLocalIdByIdentifier(resultAliases).first()
                    if (maybeExistingLocalId != null) {
                        // We already know the school by another alias, so update it
                        val existingSchool = schoolDao.findById(maybeExistingLocalId).first()!!
                        val existingAliases = existingSchool.aliases.map { it.toString() }
                        val aliasesToBeAdded = resultAliases.filter { alias -> alias.toString() !in existingAliases }
                        aliasesToBeAdded
                            .map { DbSchoolAlias.fromAlias(it, maybeExistingLocalId) }
                            .forEach { schoolDao.upsert(it) }

                        return@map schoolDao.findById(maybeExistingLocalId).first()?.toModel()
                    } else {
                        val localId = Uuid.random()
                        schoolDao.upsertSchool(
                            school = DbSchool(
                                id = localId,
                                name = result.name,
                                cachedAt = Clock.System.now(),
                                creationReason = CreationReason.Cached,
                            ),
                            aliases = resultAliases.map {
                                DbSchoolAlias.fromAlias(it, localId)
                            }
                        )
                        return@map schoolDao.findById(localId).first()?.toModel()
                    }
                } else school
            }
    }

    private fun findLocalIdByIdentifier(identifiers: Set<Alias>): Flow<Uuid?> {
        return combine(
            identifiers.map { alias ->
                schoolDao.getIdByAlias(
                    value = alias.value,
                    provider = alias.provider,
                    version = alias.version
                )
            }
        ) {
            it.firstNotNullOfOrNull { it }
        }
    }

    override suspend fun save(school: School.AppSchool) {
        val id = this.findLocalIdByIdentifier(school.aliases).first() ?: Uuid.random()
        schoolDao.upsertSchool(
            school = DbSchool(
                id = id,
                name = school.name,
                cachedAt = Clock.System.now(),
                creationReason = CreationReason.Persisted,
            ),
            aliases = school.aliases.map {
                DbSchoolAlias.fromAlias(alias = it, id = id)
            }
        )

        schoolDao.upsertSp24SchoolDetails(DbSchoolSp24Acess(
            schoolId = id,
            sp24SchoolId = school.sp24Id,
            username = school.username,
            password = school.password,
            daysPerWeek = school.daysPerWeek,
            credentialsValid = school.credentialsValid,
        ))
    }
}