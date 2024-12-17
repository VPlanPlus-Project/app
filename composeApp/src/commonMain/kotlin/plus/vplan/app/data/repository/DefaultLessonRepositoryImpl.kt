package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbDefaultLesson
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.DefaultLessonRepository

class DefaultLessonRepositoryImpl(
    private val vppDatabase: VppDatabase
) : DefaultLessonRepository {
    override fun getByGroup(groupId: Int): Flow<List<DefaultLesson>> {
        return vppDatabase.defaultLessonDao.getByGroup(groupId).map { it.map { dl -> dl.toModel() } }
    }

    override fun getById(id: String): Flow<DefaultLesson?> {
        return vppDatabase.defaultLessonDao.getById(id).map { it?.toModel() }
    }

    override suspend fun upsert(
        id: String,
        subject: String,
        groupId: Int,
        teacherId: Int?,
        courseId: String?
    ): Flow<DefaultLesson> {
        vppDatabase.defaultLessonDao.upsert(
            DbDefaultLesson(
            id = id,
            subject = subject,
            teacherId = teacherId,
            groupId = groupId,
            courseId = courseId
        )
        )
        return getById(id).map { it ?: throw IllegalStateException("upsert: defaultLesson not found") }
    }
}