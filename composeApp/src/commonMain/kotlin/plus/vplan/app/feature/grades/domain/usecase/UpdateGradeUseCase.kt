package plus.vplan.app.feature.grades.domain.usecase

import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.repository.base.ResponsePreference
import plus.vplan.app.domain.repository.besteschule.BesteSchuleGradesRepository
import plus.vplan.app.feature.assessment.domain.usecase.UpdateResult

class UpdateGradeUseCase: KoinComponent {
    private val besteSchuleGradesRepository by inject<BesteSchuleGradesRepository>()

    suspend operator fun invoke(
        gradeId: Int,
        schulverwalterAccessToken: String,
        schulverwalterUserId: Int,
    ): UpdateResult {
        val data = besteSchuleGradesRepository.getGrade(
            gradeId = gradeId,
            responsePreference = ResponsePreference.Fresh,
            contextBesteschuleAccessToken = schulverwalterAccessToken,
            contextBesteschuleUserId = schulverwalterUserId
        ).first()
        if (data !is Response.Success<BesteSchuleGrade>) return UpdateResult.ERROR
        return UpdateResult.SUCCESS
    }
}