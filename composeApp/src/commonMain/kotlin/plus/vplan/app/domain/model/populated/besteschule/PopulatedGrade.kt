@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated.besteschule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.CollectionsRepository
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade

data class PopulatedGrade(
    val grade: BesteSchuleGrade,
    val collection: BesteSchuleCollection,
)

class GradesPopulator : KoinComponent {

    private val besteSchuleIntervalsRepository by inject<IntervalsRepository>()
    private val besteSchuleCollectionsRepository by inject<CollectionsRepository>()

    fun populateMultiple(grades: List<BesteSchuleGrade>): Flow<List<PopulatedGrade>> {
        if (grades.isEmpty()) return flowOf(emptyList())
        val collections = besteSchuleCollectionsRepository.getAll()
        val intervals = besteSchuleIntervalsRepository.getAll()

        return combine(collections, intervals) { collections, intervals ->
            grades.mapNotNull { grade ->
                val collection = collections.firstOrNull { it.id == grade.collectionId } ?: return@mapNotNull null
                PopulatedGrade(
                    grade = grade,
                    collection = collection,
                )
            }
        }
    }

    fun populateSingle(grade: BesteSchuleGrade): Flow<PopulatedGrade> {
        val collection =
            besteSchuleCollectionsRepository.getById(grade.collectionId).filterNotNull()

        return collection.mapLatest { collection ->
            PopulatedGrade(
                grade = grade,
                collection = collection,
            )
        }
    }
}