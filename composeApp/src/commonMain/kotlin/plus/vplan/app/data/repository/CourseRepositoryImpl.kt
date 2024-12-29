package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbCourse
import plus.vplan.app.data.source.database.model.database.crossovers.DbCourseGroupCrossover
import plus.vplan.app.domain.model.Course
import plus.vplan.app.domain.repository.CourseRepository

class CourseRepositoryImpl(
    private val vppDatabase: VppDatabase
) : CourseRepository {
    override fun getByGroup(groupId: Int): Flow<List<Course>> {
        return vppDatabase.courseDao.getByGroup(groupId)
            .map { it.map { course -> course.toModel() } }
    }

    override fun getBySchool(schoolId: Int): Flow<List<Course>> {
        return vppDatabase.courseDao.getBySchool(schoolId)
            .map { it.map { course -> course.toModel() } }
    }

    override fun getById(id: String): Flow<Course?> {
        return vppDatabase.courseDao.getById(id).map { it?.toModel() }
    }

    override suspend fun upsert(course: Course): Flow<Course> {
        upsert(listOf(course))
        return getById(course.id).map {
            it ?: throw IllegalStateException("upsert: course not found")
        }
    }

    override suspend fun upsert(courses: List<Course>) {
        vppDatabase.courseDao.upsert(
            courses = courses.map { course ->
                DbCourse(
                    id = course.id,
                    name = course.name,
                    teacherId = course.teacher?.id
                )
            },
            courseGroupCrossovers = courses.flatMap { course ->
                course.groups.map { group ->
                    DbCourseGroupCrossover(
                        courseId = course.id,
                        groupId = group.id
                    )
                }
            }
        )
    }

    override suspend fun deleteById(id: String) {
        deleteById(listOf(id))
    }

    override suspend fun deleteById(ids: List<String>) {
        vppDatabase.courseDao.deleteById(ids)
    }
}