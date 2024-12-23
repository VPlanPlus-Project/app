package plus.vplan.app.feature.onboarding.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.feature.onboarding.data.repository.OnboardingRepositoryImpl
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.usecase.InitialiseOnboardingWithSchoolIdUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SearchForSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.UseUnknownSp24SchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearchViewModel
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.GetCurrentOnboardingSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.GetSp24CredentialsStateUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.StartIndiwareInitJobUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.OnboardingIndiwareLoginViewModel
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.usecase.TrackIndiwareProgressUseCase
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.ui.OnboardingIndiwareInitViewModel
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolDataUseCase
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.ui.OnboardingIndiwareDataDownloadViewModel
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.GetProfileOptionsUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.SelectProfileUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.OnboardingSelectProfileViewModel
import plus.vplan.app.feature.onboarding.ui.OnboardingHostViewModel

expect fun onboardingDatabaseModule(): Module

val onboardingModule = module {
    includes(onboardingDatabaseModule())

    singleOf(::OnboardingRepositoryImpl).bind<OnboardingRepository>()

    singleOf(::InitialiseOnboardingWithSchoolIdUseCase)
    singleOf(::SearchForSchoolUseCase)
    singleOf(::UseUnknownSp24SchoolUseCase)
    singleOf(::CheckCredentialsUseCase)
    singleOf(::GetSp24CredentialsStateUseCase)
    singleOf(::StartIndiwareInitJobUseCase)
    singleOf(::GetCurrentOnboardingSchoolUseCase)
    singleOf(::TrackIndiwareProgressUseCase)
    singleOf(::SetUpSchoolDataUseCase)
    singleOf(::GetProfileOptionsUseCase)
    singleOf(::SelectProfileUseCase)

    viewModelOf(::OnboardingHostViewModel)
    viewModelOf(::OnboardingSchoolSearchViewModel)
    viewModelOf(::OnboardingIndiwareLoginViewModel)
    viewModelOf(::OnboardingIndiwareInitViewModel)
    viewModelOf(::OnboardingIndiwareDataDownloadViewModel)
    viewModelOf(::OnboardingSelectProfileViewModel)
}