package plus.vplan.app.feature.profile.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.domain.usecase.UpdateAssessmentIndicesUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateIndicesUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileHomeworkIndexUseCase
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase

val profileModule = module {
    singleOf(::UpdateProfileLessonIndexUseCase)
    singleOf(::UpdateProfileHomeworkIndexUseCase)
    singleOf(::UpdateAssessmentIndicesUseCase)
    singleOf(::UpdateIndicesUseCase)
}