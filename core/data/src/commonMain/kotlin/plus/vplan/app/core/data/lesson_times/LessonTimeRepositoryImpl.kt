package plus.vplan.app.core.data.lesson_times

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbLessonTime
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.School

class LessonTimeRepositoryImpl(
    private val vppDatabase: VppDatabase
) : LessonTimeRepository {
    override fun getByGroup(group: Group): Flow<List<LessonTime>> {
        return vppDatabase.lessonTimeDao.getByGroup(group.id).map { it.map { lt -> lt.toModel() } }
    }

    override fun getByGroup(group: Group, lessonNumber: Int): Flow<LessonTime?> {
        return vppDatabase.lessonTimeDao.get(group.id, lessonNumber).map { it?.toModel() }
    }

    override fun getBySchool(school: School): Flow<List<LessonTime>> {
        return vppDatabase.lessonTimeDao.getBySchool(school.id)
            .map { it.map { lt -> lt.toModel() } }
    }

    override fun getById(id: String): Flow<LessonTime?> {
        return vppDatabase.lessonTimeDao.getById(id).map { it?.toModel() }
    }

    override suspend fun save(lessonTimes: List<LessonTime>) {
        vppDatabase.lessonTimeDao.upsert(lessonTimes.map { lessonTime ->
            DbLessonTime(
                id = lessonTime.id,
                startTime = lessonTime.start,
                endTime = lessonTime.end,
                lessonNumber = lessonTime.lessonNumber,
                groupId = lessonTime.group,
                interpolated = lessonTime.interpolated
            )
        })
    }

    override suspend fun delete(lessonTimes: List<LessonTime>) {
        vppDatabase.lessonTimeDao.deleteById(lessonTimes.map { it.id })
    }
}