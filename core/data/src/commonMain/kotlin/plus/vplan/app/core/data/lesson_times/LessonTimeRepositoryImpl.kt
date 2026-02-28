package plus.vplan.app.core.data.lesson_times

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbLessonTime
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.School
import kotlin.uuid.Uuid

class LessonTimeRepositoryImpl(
    private val vppDatabase: VppDatabase,
    private val applicationScope: CoroutineScope,
) : LessonTimeRepository {

    private val byGroupCache = mutableMapOf<Uuid, Flow<List<LessonTime>>>()
    private val bySchoolCache = mutableMapOf<Uuid, Flow<List<LessonTime>>>()

    override fun getByGroup(group: Group): Flow<List<LessonTime>> {
        return byGroupCache.getOrPut(group.id) {
            vppDatabase.lessonTimeDao.getByGroup(group.id)
                .map { it.map { lt -> lt.toModel() } }
                .distinctUntilChanged()
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getByGroup(group: Group, lessonNumber: Int): Flow<LessonTime?> {
        return vppDatabase.lessonTimeDao.get(group.id, lessonNumber)
            .map { it?.toModel() }
            .distinctUntilChanged()
    }

    override fun getBySchool(school: School): Flow<List<LessonTime>> {
        return bySchoolCache.getOrPut(school.id) {
            vppDatabase.lessonTimeDao.getBySchool(school.id)
                .map { it.map { lt -> lt.toModel() } }
                .distinctUntilChanged()
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getById(id: String): Flow<LessonTime?> {
        return vppDatabase.lessonTimeDao.getById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
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