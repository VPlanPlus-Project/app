@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbTeacher
import plus.vplan.app.data.source.database.model.database.DbTeacherAlias
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.domain.repository.TeacherDbDto
import plus.vplan.app.domain.repository.TeacherRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.Uuid

class TeacherRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TeacherRepository {
    override suspend fun upsert(item: TeacherDbDto): Uuid {
        val teacherId = resolveAliasesToLocalId(item.aliases) ?: Uuid.random()
        vppDatabase.teacherDao.upsertTeacher(
            teacher = DbTeacher(
                id = teacherId,
                name = item.name,
                cachedAt = Clock.System.now(),
                schoolId = item.schoolId
            ),
            aliases = item.aliases.map {
                DbTeacherAlias.fromAlias(it, teacherId)
            }
        )
        return teacherId
    }

    override fun getBySchool(schoolId: Uuid): Flow<List<Teacher>> {
        return vppDatabase.teacherDao.getBySchool(schoolId).map { result -> result.map { it.toModel() } }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.teacherDao.getAll()
    }

    override fun getByLocalId(id: Uuid): Flow<Teacher?> {
        return vppDatabase.teacherDao.findById(id).map { embeddedTeacher ->
            embeddedTeacher?.toModel()
        }
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.teacherDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }
}
