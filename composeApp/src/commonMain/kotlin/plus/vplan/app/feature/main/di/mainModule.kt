package plus.vplan.app.feature.main.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.main.domain.usecase.SetupApplicationUseCase
import plus.vplan.app.feature.main.domain.usecase.setup.DoAssessmentsAndHomeworkIndexMigrationUseCase
import plus.vplan.app.feature.main.domain.usecase.UpdateFirebaseTokenUseCase
import plus.vplan.app.feature.main.domain.usecase.setup.RemoveDisconnectedVppIdsFromProfilesUseCase
import plus.vplan.app.feature.system.usecase.sp24.SendSp24CredentialsToServerUseCase

val mainModule = module {
    singleOf(::DoAssessmentsAndHomeworkIndexMigrationUseCase)
    singleOf(::UpdateFirebaseTokenUseCase)
    singleOf(::SetupApplicationUseCase)
    singleOf(::RemoveDisconnectedVppIdsFromProfilesUseCase)
    singleOf(::SendSp24CredentialsToServerUseCase)
}