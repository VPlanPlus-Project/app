package plus.vplan.app.domain.model.populated.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.IntervalsRepository
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject
import plus.vplan.app.core.model.besteschule.BesteSchuleTeacher
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository

data class PopulatedCollection(
    val collection: BesteSchuleCollection,
    val interval: BesteSchuleInterval,
    val teacher: BesteSchuleTeacher,
    val subject: BesteSchuleSubject,
)

class CollectionPopulator: KoinComponent {

    private val besteSchuleIntervalsRepository by inject<IntervalsRepository>()
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()
    private val besteSchuleTeachersRepository by inject<BesteSchuleTeachersRepository>()

    fun populateSingle(collection: BesteSchuleCollection): Flow<PopulatedCollection> {
        val intervals = besteSchuleIntervalsRepository.getById(collection.intervalId).filterNotNull()
        val subject = besteSchuleSubjectsRepository.getSubjectFromCache(collection.subjectId).filterNotNull()
        val teacher = besteSchuleTeachersRepository.getTeacherFromCache(collection.teacherId).filterNotNull()

        return combine(
            intervals,
            subject,
            teacher
        ) { interval, subject, teacher ->
            PopulatedCollection(
                collection = collection,
                interval = interval,
                teacher = teacher,
                subject = subject
            )
        }
    }
}
