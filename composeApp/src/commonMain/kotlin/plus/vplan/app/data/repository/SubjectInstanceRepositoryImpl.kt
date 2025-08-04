package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbSubjectInstance
import plus.vplan.app.data.source.database.model.database.DbSubjectInstanceAlias
import plus.vplan.app.data.source.database.model.database.foreign_key.FKSubjectInstanceGroup
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceDbDto
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import kotlin.uuid.Uuid

class SubjectInstanceRepositoryImpl(
    private val vppDatabase: VppDatabase
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
}
