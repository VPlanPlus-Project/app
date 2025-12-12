package plus.vplan.app.feature.grades.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.grades.domain.usecase.UpdateGradeUseCase
import plus.vplan.app.feature.grades.page.analytics.ui.AnalyticsViewModel
import plus.vplan.app.feature.grades.page.detail.ui.GradeDetailViewModel
import plus.vplan.app.feature.grades.page.view.ui.GradesViewModel

val gradeModule = module {
    singleOf(::UpdateGradeUseCase)
    singleOf(::CalculateAverageUseCase)
    singleOf(::GetGradeLockStateUseCase)
    singleOf(::LockGradesUseCase)
    singleOf(::RequestGradeUnlockUseCase)

    viewModelOf(::GradeDetailViewModel)
    viewModelOf(::GradesViewModel)
    viewModelOf(::AnalyticsViewModel)
}