package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.repository.LessonTimeRepository

class LessonTimeRepositoryImpl(
    private val vppDatabase: VppDatabase
) : LessonTimeRepository {
    override fun getByGroup(groupId: Int): Flow<List<LessonTime>> {
        return vppDatabase.lessonTimeDao.getByGroup(groupId).map { it.map { lt -> lt.toModel() } }
    }

    override fun getBySchool(schoolId: Int): Flow<List<LessonTime>> {
        return vppDatabase.lessonTimeDao.getBySchool(schoolId).map { it.map { lt -> lt.toModel() } }
    }

    override fun getById(id: String): Flow<LessonTime?> {
        return vppDatabase.lessonTimeDao.getById(id).map { it?.toModel() }
    }

    override suspend fun upsert(lessonTime: LessonTime): Flow<LessonTime> {
        upsert(listOf(lessonTime))
        return getById(lessonTime.id).map { it ?: throw IllegalStateException("upsert: lessonTime not found") }
    }

    override suspend fun upsert(lessonTimes: List<LessonTime>) {
        vppDatabase.lessonTimeDao.upsert(lessonTimes.map { lessonTime ->
            DbLessonTime(
                id = lessonTime.id,
                startTime = lessonTime.start,
                endTime = lessonTime.end,
                lessonNumber = lessonTime.lessonNumber,
                groupId = lessonTime.group.getItemId().toInt(),
                interpolated = lessonTime.interpolated
            )
        })
    }

    override suspend fun deleteById(id: String) {
        vppDatabase.lessonTimeDao.deleteById(id)
    }

    override suspend fun deleteById(ids: List<String>) {
        vppDatabase.lessonTimeDao.deleteById(ids)
    }
}