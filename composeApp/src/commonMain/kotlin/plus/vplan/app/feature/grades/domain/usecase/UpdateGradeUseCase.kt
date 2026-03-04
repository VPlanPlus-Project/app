package plus.vplan.app.feature.grades.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.data.besteschule.GradesRepository
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult

class UpdateGradeUseCase: KoinComponent {
    private val besteSchuleGradesRepository by inject<GradesRepository>()

    suspend operator fun invoke(gradeId: Int): UpdateResult {
        return besteSchuleGradesRepository.getById(
            id = gradeId,
            forceRefresh = true
        )
            .map { if (it == null) UpdateResult.DOES_NOT_EXIST else UpdateResult.SUCCESS }
            .catch { e ->
                Logger.e { "Error updating grade $gradeId: ${e.stackTraceToString()}" }
                emit(UpdateResult.ERROR)
            }
            .first()
    }
}