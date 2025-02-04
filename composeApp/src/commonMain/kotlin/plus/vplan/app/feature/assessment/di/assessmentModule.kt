package plus.vplan.app.feature.assessment.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.assessment.domain.usecase.CreateAssessmentUseCase
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentViewModel
import plus.vplan.app.feature.assessment.ui.components.detail.DetailViewModel

val assessmentModule = module {
    singleOf(::CreateAssessmentUseCase)

    viewModelOf(::NewAssessmentViewModel)
    viewModelOf(::DetailViewModel)
}