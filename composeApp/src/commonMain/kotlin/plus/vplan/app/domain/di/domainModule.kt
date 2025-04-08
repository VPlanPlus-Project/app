package plus.vplan.app.domain.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.domain.usecase.CheckEMailStructureUseCase
import plus.vplan.app.domain.usecase.GetCurrentDateTimeUseCase
import plus.vplan.app.domain.usecase.GetDayUseCase
import plus.vplan.app.domain.usecase.GetProfileByIdUseCase
import plus.vplan.app.domain.usecase.OnNotificationGrantedUseCase
import plus.vplan.app.domain.usecase.SetCurrentProfileUseCase
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase

val domainModule = module {
    singleOf(::GetCurrentDateTimeUseCase)
    singleOf(::SetCurrentProfileUseCase)
    singleOf(::GetDayUseCase)
    singleOf(::GetProfileByIdUseCase)
    singleOf(::OnNotificationGrantedUseCase)
    singleOf(::CheckEMailStructureUseCase)
    singleOf(::UpdateFirebaseTokenUseCase)
}