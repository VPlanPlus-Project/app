@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated.besteschule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.IntervalRepository
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.repository.besteschule.BesteSchuleCollectionsRepository

data class PopulatedGrade(
    val grade: BesteSchuleGrade,
    val interval: BesteSchuleInterval,
    val collection: BesteSchuleCollection,
)

class GradesPopulator : KoinComponent {

    private val besteSchuleIntervalsRepository by inject<IntervalRepository>()
    private val besteSchuleCollectionsRepository by inject<BesteSchuleCollectionsRepository>()

    fun populateMultiple(grades: List<BesteSchuleGrade>): Flow<List<PopulatedGrade>> {
        if (grades.isEmpty()) return flowOf(emptyList())
        val collections = besteSchuleCollectionsRepository.getAllFromCache()
        val intervals = besteSchuleIntervalsRepository.getAll()

        return combine(collections, intervals) { collections, intervals ->
            grades.mapNotNull { grade ->
                val collection = collections.firstOrNull { it.id == grade.collectionId } ?: return@mapNotNull null
                val interval = intervals.firstOrNull { it.id == collection.intervalId } ?: return@mapNotNull null
                PopulatedGrade(
                    grade = grade,
                    collection = collection,
                    interval = interval
                )
            }
        }
    }

    fun populateSingle(grade: BesteSchuleGrade): Flow<PopulatedGrade> {
        val collection =
            besteSchuleCollectionsRepository.getFromCache(grade.collectionId).filterNotNull()

        return collection.flatMapLatest { collection ->
            besteSchuleIntervalsRepository
                .getById(collection.intervalId)
                .filterNotNull().mapLatest { interval ->
                    PopulatedGrade(
                        grade = grade,
                        interval = interval,
                        collection = collection,
                    )
                }
        }
    }
}