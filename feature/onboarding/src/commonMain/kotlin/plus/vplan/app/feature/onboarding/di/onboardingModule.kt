package plus.vplan.app.feature.onboarding.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.onboarding.OnboardingViewModel
import plus.vplan.app.feature.onboarding.stage.loading_data.domain.usecase.FetchProfileOptionsUseCase
import plus.vplan.app.feature.onboarding.stage.school_credentials.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.Stundenplan24CredentialsViewModel
import plus.vplan.app.feature.onboarding.stage.school_select.domain.usecase.SearchForSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.school_select.ui.SchoolSearchViewModel

val onboardingModule = module {
    singleOf(::SearchForSchoolUseCase)
    singleOf(::CheckCredentialsUseCase)
    singleOf(::FetchProfileOptionsUseCase)

    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SchoolSearchViewModel)
    viewModelOf(::Stundenplan24CredentialsViewModel)
}