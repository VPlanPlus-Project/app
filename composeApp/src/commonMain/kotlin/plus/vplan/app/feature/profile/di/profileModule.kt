package plus.vplan.app.feature.profile.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.domain.usecase.GetProfilesUseCase
import plus.vplan.app.feature.profile.domain.usecase.HasVppIdLinkedUseCase
import plus.vplan.app.feature.profile.domain.usecase.SetProfileDefaultLessonEnabledUseCase
import plus.vplan.app.feature.profile.ui.ProfileViewModel

val profileModule = module {
    singleOf(::GetProfilesUseCase)
    singleOf(::SetProfileDefaultLessonEnabledUseCase)
    singleOf(::HasVppIdLinkedUseCase)

    viewModelOf(::ProfileViewModel)
}