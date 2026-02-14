package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleTeacher
import plus.vplan.app.domain.model.besteschule.BesteSchuleTeacher
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository

class BesteSchuleTeachersRepositoryImpl : BesteSchuleTeachersRepository, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleTeacher?>>()

    override suspend fun addTeachersToCache(items: List<BesteSchuleTeacher>) = withContext(Dispatchers.IO) {
        vppDatabase.besteSchuleTeacherDao.upsert(items.map { teacher ->
            DbBesteschuleTeacher(
                id = teacher.id,
                localId = teacher.localId,
                forename = teacher.forename,
                surname = teacher.surname,
                cachedAt = teacher.cachedAt
            )
        })
    }

    override fun getTeacherFromCache(teacherId: Int): Flow<BesteSchuleTeacher?> {
        return cacheFlows.getOrPut(teacherId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleTeacherDao.getTeacher(teacherId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000, // when last subscriber leaves, wait 5s then stop
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1 // last value available immediately for new subscribers
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(teacherId)
            }

            shared
        }
    }
}