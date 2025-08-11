package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.DbCourseAlias
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseDbDto
import plus.vplan.app.domain.repository.CourseRepository
import kotlin.uuid.Uuid

class CourseRepositoryImpl(
    private val vppDatabase: VppDatabase,
) : CourseRepository {
    override fun getByGroup(groupId: Uuid): Flow<List<Course>> {
        return vppDatabase.courseDao.getByGroup(groupId)
            .map { it.map { course -> course.toModel() } }
    }

    override fun getAllLocalIds(): Flow<List<Uuid>> {
        return vppDatabase.courseDao.getAll().map { it.map { course -> course.course.id } }
    }

    override fun getByLocalId(id: Uuid): Flow<Course?> {
        return vppDatabase.courseDao.findById(id).map { it?.toModel() }
    }

    override fun getBySchool(schoolId: Uuid): Flow<List<Course>> {
        return vppDatabase.courseDao.getBySchool(schoolId).map { it.map { course -> course.toModel() } }
    }

    override suspend fun deleteById(id: Uuid) {
        deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<Uuid>) {
        vppDatabase.courseDao.deleteById(ids)
    }

    override suspend fun upsert(item: CourseDbDto): Uuid {
        val courseId = resolveAliasesToLocalId(item.aliases) ?: Uuid.random()
        vppDatabase.courseDao.upsertCourse(
            course = DbCourse(
                id = courseId,
                name = item.name,
                cachedAt = Clock.System.now(),
                teacherId = item.teacher
            ),
            aliases = item.aliases.map {
                DbCourseAlias.fromAlias(it, courseId)
            },
            courseGroupCrossover = item.groups.map { groupId ->
                DbCourseGroupCrossover(courseId, groupId)
            }
        )
        return courseId
    }

    override suspend fun resolveAliasToLocalId(alias: Alias): Uuid? {
        return vppDatabase.courseDao.getIdByAlias(alias.value, alias.provider, alias.version)
    }
}
