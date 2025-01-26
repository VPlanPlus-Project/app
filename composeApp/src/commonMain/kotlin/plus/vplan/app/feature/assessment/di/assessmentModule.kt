package plus.vplan.app.feature.assessment.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentViewModel

val assessmentModule = module {
    viewModelOf(::NewAssessmentViewModel)
}