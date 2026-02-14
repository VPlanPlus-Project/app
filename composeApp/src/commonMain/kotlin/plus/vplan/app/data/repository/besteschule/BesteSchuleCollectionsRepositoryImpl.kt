package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.domain.model.besteschule.BesteSchuleCollection
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository

class BesteSchuleCollectionsRepositoryImpl : BesteSchuleCollectionsRepository, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleCollection?>>()

    override suspend fun addCollectionsToCache(items: Set<BesteSchuleCollection>) = withContext(Dispatchers.IO) {
        val existingIntervals = vppDatabase.besteSchuleIntervalDao.getAll().first().map { it.interval.id }
        val existingTeachers = vppDatabase.besteSchuleTeacherDao.getAll().first().map { it.id }
        val existingSubjects = vppDatabase.besteSchuleSubjectDao.getAll().first().map { it.id }

        val items = items
            .filter { it.teacherId in existingTeachers }
            .filter { it.subjectId in existingSubjects }
            .filter { it.intervalId in existingIntervals }

        vppDatabase.besteSchuleCollectionDao.upsert(items.map { collection ->
            DbBesteSchuleCollection(
                id = collection.id,
                type = collection.type,
                name = collection.name,
                subjectId = collection.subjectId,
                givenAt = collection.givenAt,
                intervalId = collection.intervalId,
                teacherId = collection.teacherId,
                cachedAt = collection.cachedAt
            )
        })
    }

    override fun getFromCache(collectionId: Int): Flow<BesteSchuleCollection?> {
        return cacheFlows.getOrPut(collectionId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleCollectionDao.getById(collectionId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(collectionId)
            }

            shared
        }
    }
}