package plus.vplan.app.domain.model.besteschule

import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.repository.besteschule.BesteSchuleIntervalsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleSubjectsRepository
import plus.vplan.app.domain.repository.besteschule.BesteSchuleTeachersRepository
import kotlin.getValue
import kotlin.time.Instant

data class BesteSchuleCollection(
    val id: Int,
    val type: String,
    val name: String,
    val subjectId: Int,
    val givenAt: LocalDate,
    val intervalId: Int,
    val teacherId: Int,
    val cachedAt: Instant
): KoinComponent {
    private val besteSchuleSubjectsRepository by inject<BesteSchuleSubjectsRepository>()
    private val besteSchuleIntervalsRepository by inject<BesteSchuleIntervalsRepository>()
    private val besteSchuleTeachersRepository by inject<BesteSchuleTeachersRepository>()

    val subject by lazy { besteSchuleSubjectsRepository.getSubjectFromCache(subjectId) }
    val interval by lazy { besteSchuleIntervalsRepository.getIntervalFromCache(intervalId) }
    val teacher by lazy { besteSchuleTeachersRepository.getTeacherFromCache(teacherId) }
}