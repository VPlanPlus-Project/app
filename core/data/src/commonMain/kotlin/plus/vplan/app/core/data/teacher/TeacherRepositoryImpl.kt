@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.teacher

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.TeacherDao
import plus.vplan.app.core.database.model.database.DbTeacher
import plus.vplan.app.core.database.model.database.DbTeacherAlias
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Teacher
import kotlin.time.Clock
import kotlin.uuid.Uuid

class TeacherRepositoryImpl(
    private val teacherDao: TeacherDao,
): TeacherRepository {
    override fun getByIds(identifiers: Set<Alias>): Flow<Teacher?> {
        return combine(identifiers.map { teacherDao.getIdByAlias(it.value, it.provider, it.version) }) { uuids ->
            uuids.firstNotNullOfOrNull { it }
        }.distinctUntilChanged().flatMapLatest { teacherId ->
            if (teacherId == null) flowOf(null)
            else teacherDao.findById(teacherId).map { it?.toModel() }
        }
    }

    override fun getBySchool(school: School): Flow<List<Teacher>> {
        return teacherDao.getBySchool(school.id).map { teachers -> teachers.map { it.toModel() } }
    }

    override fun getAll(): Flow<List<Teacher>> {
        return teacherDao.getAll().map { teachers -> teachers.map { it.toModel() } }
    }

    @Deprecated("Use aliases")
    override fun getByLocalId(id: Uuid): Flow<Teacher?> {
        return teacherDao.findById(id).map { it?.toModel() }
    }

    override suspend fun save(teacher: Teacher) {
        teacherDao.upsertTeacher(
            teacher = DbTeacher(
                id = teacher.id,
                schoolId = teacher.school.id,
                name = teacher.name,
                cachedAt = Clock.System.now()
            ),
            aliases = teacher.aliases.map { DbTeacherAlias.fromAlias(it, teacher.id) }
        )
    }

}