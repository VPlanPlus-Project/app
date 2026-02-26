@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.course

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.dao.CourseDao
import plus.vplan.app.core.database.model.database.DbCourse
import plus.vplan.app.core.database.model.database.DbCourseAlias
import plus.vplan.app.core.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
) : CourseRepository {
    override fun getByIds(identifiers: Set<Alias>): Flow<Course?> {
        return combine(identifiers.map { courseDao.getIdByAlias(it.value, it.provider, it.version) }) { uuids ->
            uuids.firstNotNullOfOrNull { it }
        }.distinctUntilChanged().flatMapLatest { id ->
            if (id == null) flowOf(null)
            else courseDao.findById(id).map { it?.toModel() }
        }
    }

    override fun getBySchool(school: School): Flow<List<Course>> {
        return courseDao.getBySchool(school.id).map { courses -> courses.map { it.toModel() } }
    }

    override fun getByGroup(group: Group): Flow<List<Course>> {
        return courseDao.getByGroup(group.id).map { items -> items.map { it.toModel() } }
    }

    override fun getAll(): Flow<List<Course>> {
        return courseDao.getAll().map { courses -> courses.map { it.toModel() } }
    }

    @Deprecated("Use alias")
    override fun getByLocalId(id: Uuid): Flow<Course?> {
        return courseDao.findById(id).map { it?.toModel() }
    }

    override suspend fun save(course: Course) {
        courseDao.upsertCourse(
            course = DbCourse(
                id = course.id,
                name = course.name,
                teacherId = course.teacher?.id,
                cachedAt = course.cachedAt
            ),
            courseGroupCrossover = course.groups.map {
                DbCourseGroupCrossover(course.id, it.id)
            },
            aliases = course.aliases.map { DbCourseAlias.fromAlias(it, course.id) }
        )
    }

    override suspend fun delete(courses: List<Course>) {
        courseDao.deleteById(courses.map { it.id })
    }
}