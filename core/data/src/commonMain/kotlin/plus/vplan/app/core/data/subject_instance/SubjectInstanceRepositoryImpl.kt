@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.subject_instance

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import plus.vplan.app.core.database.dao.SubjectInstanceDao
import plus.vplan.app.core.database.model.database.DbSubjectInstance
import plus.vplan.app.core.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.core.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.network.vpp.subject_instance.SubjectInstanceApi
import kotlin.time.Clock
import kotlin.uuid.Uuid

class SubjectInstanceRepositoryImpl(
    private val subjectInstanceDao: SubjectInstanceDao,
    private val subjectInstanceApi: SubjectInstanceApi,
    private val applicationScope: CoroutineScope,
): SubjectInstanceRepository {

    private val bySchoolCache = mutableMapOf<Uuid, Flow<List<SubjectInstance>>>()
    private val byGroupCache = mutableMapOf<Uuid, Flow<List<SubjectInstance>>>()
    private val allCache: Flow<List<SubjectInstance>> by lazy {
        subjectInstanceDao.getAll()
            .map { items -> items.map { it.toModel() } }
            .distinctUntilChanged()
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
    }

    override fun getByIds(
        identifiers: Set<Alias>,
        forceUpdate: Boolean
    ): Flow<SubjectInstance?> {
        return combine(identifiers.map { subjectInstanceDao.getIdByAlias(it.value, it.provider, it.version) }) { uuids ->
            uuids.firstNotNullOfOrNull { it }
        }.distinctUntilChanged().flatMapLatest { id ->
            if (id == null) flowOf(null)
            else subjectInstanceDao.findById(id).map { it?.toModel() }.distinctUntilChanged()
        }.map { subjectInstance ->
            if (subjectInstance == null || forceUpdate) {
                val response = subjectInstanceApi.getByAlias(identifiers.first())
                    ?: return@map null

                val responseAliases = response.aliases.mapNotNull { it.toModel() }.toSet()

                val localId = findLocalIdByIdentifier(responseAliases).first()
                    ?: return@map null

                val item = subjectInstance ?: subjectInstanceDao.findById(localId).first()!!.toModel()

                val missingAliases = responseAliases.filter { alias ->
                    item.aliases.none { it.toString() == alias.toString() }
                }

                missingAliases.forEach { alias ->
                    subjectInstanceDao.upsert(DbSubjectInstanceAlias.fromAlias(alias, localId))
                }

                getByIds(identifiers).first()!!
            } else subjectInstance
        }
    }

    private fun findLocalIdByIdentifier(identifiers: Set<Alias>): Flow<Uuid?> {
        return combine(
            identifiers.map { alias ->
                subjectInstanceDao.getIdByAlias(
                    value = alias.value,
                    provider = alias.provider,
                    version = alias.version
                )
            }
        ) { ids ->
            ids.firstNotNullOfOrNull { it }
        }
    }

    override fun getByGroup(group: Group): Flow<List<SubjectInstance>> {
        return byGroupCache.getOrPut(group.id) {
            subjectInstanceDao.getByGroup(group.id)
                .map { items -> items.map { it.toModel() } }
                .distinctUntilChanged()
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getByTeacher(teacher: Teacher): Flow<List<SubjectInstance>> {
        return subjectInstanceDao.getByTeacher(teacher.id)
            .map { items -> items.map { it.toModel() } }
            .distinctUntilChanged()
    }

    override fun getBySchool(school: School): Flow<List<SubjectInstance>> {
        return bySchoolCache.getOrPut(school.id) {
            subjectInstanceDao.getBySchool(school.id)
                .map { items -> items.map { it.toModel() } }
                .distinctUntilChanged()
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getAll(): Flow<List<SubjectInstance>> = allCache

    @Deprecated("Use alias")
    override fun getByLocalId(id: Uuid): Flow<SubjectInstance?> {
        return subjectInstanceDao.findById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
    }

    override suspend fun save(subjectInstance: SubjectInstance) {
        val id = findLocalIdByIdentifier(subjectInstance.aliases.toSet()).first() ?: Uuid.random()
        subjectInstanceDao.upsertSubjectInstance(
            entity = DbSubjectInstance(
                id = id,
                subject = subjectInstance.subject,
                teacherId = subjectInstance.teacher?.id,
                courseId = subjectInstance.course?.id,
                cachedAt = Clock.System.now()
            ),
            groups = subjectInstance.groups.map {
                FKSubjectInstanceGroup(id, it.id)
            },
            aliases = subjectInstance.aliases.map {
                DbSubjectInstanceAlias.fromAlias(it, id)
            }
        )
    }

    override suspend fun delete(subjectInstances: List<SubjectInstance>) {
        subjectInstanceDao.deleteById(subjectInstances.map { it.id })
    }
}