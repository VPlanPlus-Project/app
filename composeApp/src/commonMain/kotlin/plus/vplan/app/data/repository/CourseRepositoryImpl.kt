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
        return vppDatabase.courseDao.getByGroup(groupId).map { it.map { it.toModel() } }
    }

    override fun getBypId(id: String): Flow<Course?> {
        return vppDatabase.courseDao.getById(id).map { it?.toModel() }
    }

    override suspend fun upsert(
        id: String,
        name: String,
        groupId: Int,
        teacherId: Int?
    ): Flow<Course> {
        vppDatabase.courseDao.upsert(DbCourse(
            id = id,
            name = name,
            teacherId = teacherId,
            groupId = groupId
        ))
        return getBypId(id).map { it ?: throw IllegalStateException("upsert: course not found") }
    }
}