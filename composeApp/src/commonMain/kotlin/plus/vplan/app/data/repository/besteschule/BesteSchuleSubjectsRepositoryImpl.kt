package plus.vplan.app.data.repository.besteschule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.besteschule.DbBesteschuleSubject
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class BesteSchuleSubjectsRepositoryImpl : BesteSchuleSubjectsRepository, KoinComponent {
    private val vppDatabase by inject<VppDatabase>()

    private val cacheFlows = mutableMapOf<Int, Flow<BesteSchuleSubject?>>()

    override suspend fun addSubjectsToCache(subjects: Set<BesteSchuleSubject>) = withContext(Dispatchers.IO) {
        vppDatabase.besteSchuleSubjectDao.upsert(subjects.map { subject ->
            DbBesteschuleSubject(
                id = subject.id,
                shortName = subject.shortName,
                longName = subject.fullName,
                cachedAt = subject.cachedAt
            )
        })
    }

    override fun getSubjectFromCache(subjectId: Int): Flow<BesteSchuleSubject?> {
        return cacheFlows.getOrPut(subjectId) {
            val upstreamScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            val shared = vppDatabase.besteSchuleSubjectDao.getById(subjectId).map { it?.toModel() }
                .shareIn(
                    upstreamScope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = 5_000,
                        replayExpirationMillis = Long.MAX_VALUE
                    ),
                    replay = 1
                )

            upstreamScope.coroutineContext.job.invokeOnCompletion {
                cacheFlows.remove(subjectId)
            }

            shared
        }
    }
}