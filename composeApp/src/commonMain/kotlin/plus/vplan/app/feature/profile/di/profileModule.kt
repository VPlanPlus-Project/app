package plus.vplan.app.feature.profile.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.domain.usecase.UpdateIndicesUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileAssessmentIndexUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase

val profileModule = module {
    singleOf(::UpdateProfileHomeworkIndexUseCase)
    singleOf(::UpdateProfileAssessmentIndexUseCase)
    singleOf(::UpdateIndicesUseCase)
}