package plus.vplan.app.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.assessment.detail.ui.AssessmentDetailViewModel
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentContentUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentDateUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentTypeUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.ChangeAssessmentVisibilityUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.DeleteAssessmentUseCase
import plus.vplan.app.feature.assessment.core.domain.usecase.RefreshAssessmentUseCase
import plus.vplan.app.feature.assessment.create.domain.usecase.CreateAssessmentUseCase
import plus.vplan.app.feature.assessment.create.ui.NewAssessmentViewModel

val assessmentModule = module {
    singleOf(::CreateAssessmentUseCase)
    singleOf(::RefreshAssessmentUseCase)
    singleOf(::DeleteAssessmentUseCase)
    singleOf(::ChangeAssessmentTypeUseCase)
    singleOf(::ChangeAssessmentDateUseCase)
    singleOf(::ChangeAssessmentVisibilityUseCase)
    singleOf(::ChangeAssessmentContentUseCase)
    // Note: File use cases are now in domainModule (generic, not assessment-specific)

    viewModelOf(::NewAssessmentViewModel)
    viewModelOf(::AssessmentDetailViewModel)
}