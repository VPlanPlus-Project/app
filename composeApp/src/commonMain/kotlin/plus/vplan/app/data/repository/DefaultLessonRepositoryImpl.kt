package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbDefaultLessonGroupCrossover
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonRepositoryImpl(
    private val vppDatabase: VppDatabase
) : DefaultLessonRepository {
    override fun getByGroup(groupId: Int): Flow<List<DefaultLesson>> {
        return vppDatabase.defaultLessonDao.getByGroup(groupId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getBySchool(schoolId: Int): Flow<List<DefaultLesson>> {
        return vppDatabase.defaultLessonDao.getBySchool(schoolId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getById(id: String): Flow<DefaultLesson?> {
        return vppDatabase.defaultLessonDao.getById(id).map { it?.toModel() }
    }

    override suspend fun upsert(defaultLesson: DefaultLesson): Flow<DefaultLesson> {
        upsert(listOf(defaultLesson))
        return getById(defaultLesson.id).map { it ?: throw IllegalStateException("upsert: defaultLesson not found") }
    }

    override suspend fun upsert(defaultLessons: List<DefaultLesson>) {
        vppDatabase.defaultLessonDao.upsert(
            defaultLessons = defaultLessons.map { defaultLesson ->
                DbDefaultLesson(
                    id = defaultLesson.id,
                    subject = defaultLesson.subject,
                    teacherId = defaultLesson.teacher?.id,
                    courseId = defaultLesson.course?.id
                )
            },
            defaultLessonGroupCrossovers = defaultLessons.flatMap { defaultLesson ->
                defaultLesson.groups.map { group ->
                    DbDefaultLessonGroupCrossover(
                        defaultLessonId = defaultLesson.id,
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
        vppDatabase.defaultLessonDao.deleteById(ids)
    }
}