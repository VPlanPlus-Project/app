package plus.vplan.app.feature.profile.page.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.profile.page.domain.usecase.GetCurrentProfileUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.GetProfilesUseCase
import plus.vplan.app.feature.profile.page.domain.usecase.HasVppIdLinkedUseCase
import plus.vplan.app.feature.profile.page.ui.ProfileViewModel

val profilePageModule = module {
    singleOf(::GetProfilesUseCase)
    singleOf(::GetCurrentProfileUseCase)
    singleOf(::HasVppIdLinkedUseCase)

    viewModelOf(::ProfileViewModel)
}