package plus.vplan.app.feature.profile.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase

val profileModule = module {
    singleOf(::UpdateProfileLessonIndexUseCase)
}