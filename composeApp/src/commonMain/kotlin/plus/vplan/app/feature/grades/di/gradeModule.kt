package plus.vplan.app.feature.grades.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.grades.domain.usecase.CalculateAverageUseCase
import plus.vplan.app.feature.grades.domain.usecase.GetCurrentIntervalUseCase
import plus.vplan.app.feature.grades.domain.usecase.ToggleConsiderGradeForFinalGradeUseCase
import plus.vplan.app.feature.grades.domain.usecase.UpdateGradeUseCase
import plus.vplan.app.feature.grades.ui.components.detail.GradeDetailViewModel

val gradeModule = module {
    singleOf(::UpdateGradeUseCase)
    singleOf(::ToggleConsiderGradeForFinalGradeUseCase)
    singleOf(::CalculateAverageUseCase)
    singleOf(::GetCurrentIntervalUseCase)

    viewModelOf(::GradeDetailViewModel)
}