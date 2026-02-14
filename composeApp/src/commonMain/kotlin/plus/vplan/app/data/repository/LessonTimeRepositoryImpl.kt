package plus.vplan.app.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbLessonTime
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.repository.LessonTimeRepository
import kotlin.uuid.Uuid

class LessonTimeRepositoryImpl(
    private val vppDatabase: VppDatabase
) : LessonTimeRepository {

    private val getByGroupFlows = mutableMapOf<Uuid, Flow<List<LessonTime>>>()
    private val getFlows = mutableMapOf<Pair<Uuid, Int>, Flow<LessonTime?>>()
    private val getBySchoolFlows = mutableMapOf<Uuid, Flow<List<LessonTime>>>()
    private val getByIdFlows = mutableMapOf<String, Flow<LessonTime?>>()

    override fun getByGroup(groupId: Uuid): Flow<List<LessonTime>> {
        return getByGroupFlows.getOrPut(groupId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.lessonTimeDao.getByGroup(groupId).map { it.map { lt -> lt.toModel() } }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getByGroupFlows.remove(groupId)
            }

            shared
        }
    }

    override fun get(groupId: Uuid, lessonNumber: Int): Flow<LessonTime?> {
        val key = groupId to lessonNumber
        return getFlows.getOrPut(key) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.lessonTimeDao.get(groupId, lessonNumber).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getFlows.remove(key)
            }

            shared
        }
    }

    override fun getBySchool(schoolId: Uuid): Flow<List<LessonTime>> {
        return getBySchoolFlows.getOrPut(schoolId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.lessonTimeDao.getBySchool(schoolId).map { it.map { lt -> lt.toModel() } }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getBySchoolFlows.remove(schoolId)
            }

            shared
        }
    }

    override fun getById(id: String): Flow<LessonTime?> {
        return getByIdFlows.getOrPut(id) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.lessonTimeDao.getById(id).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                getByIdFlows.remove(id)
            }

            shared
        }
    }

    override suspend fun upsert(lessonTime: LessonTime) {
        upsert(listOf(lessonTime))
    }

    override suspend fun upsert(lessonTimes: List<LessonTime>) {
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

    override suspend fun deleteById(id: String) {
        vppDatabase.lessonTimeDao.deleteById(id)
    }

    override suspend fun deleteById(ids: List<String>) {
        vppDatabase.lessonTimeDao.deleteById(ids)
    }
}