@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.domain.model.populated.besteschule

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.TeachersRepository
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection
import plus.vplan.app.core.model.besteschule.BesteSchuleTeacher

data class PopulatedCollection(
    val collection: BesteSchuleCollection,
    val teacher: BesteSchuleTeacher,
)

class CollectionPopulator: KoinComponent {
    private val besteSchuleTeachersRepository by inject<TeachersRepository>()

    fun populateSingle(collection: BesteSchuleCollection): Flow<PopulatedCollection> {
        val teacher = besteSchuleTeachersRepository.getById(collection.teacherId).filterNotNull()

        return teacher.mapLatest { teacher ->
            PopulatedCollection(
                collection = collection,
                teacher = teacher,
            )
        }
    }
}
