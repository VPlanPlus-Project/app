package plus.vplan.app.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.grades.common.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.GetGradeLockStateUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.LockGradesUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.RequestGradeUnlockUseCase
import plus.vplan.app.feature.grades.common.domain.usecase.UpdateGradeUseCase
import plus.vplan.app.feature.grades.detail.ui.GradeDetailViewModel
import plus.vplan.app.feature.grades.list.ui.GradesViewModel
import plus.vplan.app.feature.grades.ui.AnalyticsViewModel

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