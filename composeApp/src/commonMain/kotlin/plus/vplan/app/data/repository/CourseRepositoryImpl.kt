package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseRepository

class CourseRepositoryImpl(
    private val vppDatabase: VppDatabase
) : CourseRepository {
    override fun getByGroup(groupId: Int): Flow<List<Course>> {
        return vppDatabase.courseDao.getByGroup(groupId).map { it.map { course -> course.toModel() } }
    }

    override fun getBypId(id: String): Flow<Course?> {
        return vppDatabase.courseDao.getById(id).map { it?.toModel() }
    }

    override suspend fun upsert(course: Course): Flow<Course> {
        vppDatabase.courseDao.upsert(
            DbCourse(
                id = course.id,
                name = course.name,
                teacherId = course.teacher?.id,
                groupId = course.group.id
            )
        )
        return getBypId(course.id).map { it ?: throw IllegalStateException("upsert: course not found") }
    }
}