package plus.vplan.app.feature.onboarding.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.feature.onboarding.data.repository.OnboardingRepositoryImpl
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.SearchForSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.UseUnknownSp24SchoolUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearchViewModel
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.GetCurrentOnboardingSchoolUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.GetSp24CredentialsStateUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.OnboardingIndiwareLoginViewModel

expect fun onboardingDatabaseModule(): Module

val onboardingModule = module {
    includes(onboardingDatabaseModule())

    singleOf(::OnboardingRepositoryImpl).bind<OnboardingRepository>()

    singleOf(::SearchForSchoolUseCase)
    singleOf(::UseUnknownSp24SchoolUseCase)
    singleOf(::CheckCredentialsUseCase)
    singleOf(::GetSp24CredentialsStateUseCase)
    singleOf(::GetCurrentOnboardingSchoolUseCase)

    viewModelOf(::OnboardingSchoolSearchViewModel)
    viewModelOf(::OnboardingIndiwareLoginViewModel)
}