package plus.vplan.app.feature.assessment.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentContentUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentTypeUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentDateUseCase
import plus.vplan.app.feature.assessment.domain.usecase.ChangeAssessmentVisibilityUseCase
import plus.vplan.app.feature.assessment.domain.usecase.CreateAssessmentUseCase
import plus.vplan.app.feature.assessment.domain.usecase.DeleteAssessmentUseCase
import plus.vplan.app.feature.assessment.domain.usecase.UpdateAssessmentUseCase
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentViewModel
import plus.vplan.app.feature.assessment.ui.components.detail.DetailViewModel

val assessmentModule = module {
    singleOf(::CreateAssessmentUseCase)
    singleOf(::UpdateAssessmentUseCase)
    singleOf(::DeleteAssessmentUseCase)
    singleOf(::ChangeAssessmentTypeUseCase)
    singleOf(::ChangeAssessmentDateUseCase)
    singleOf(::ChangeAssessmentVisibilityUseCase)
    singleOf(::ChangeAssessmentContentUseCase)

    viewModelOf(::NewAssessmentViewModel)
    viewModelOf(::DetailViewModel)
}