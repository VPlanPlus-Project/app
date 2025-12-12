package plus.vplan.app.feature.schulverwalter.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.schulverwalter.SchulverwalterRepositoryImpl
import plus.vplan.app.domain.repository.schulverwalter.SchulverwalterRepository
import plus.vplan.app.feature.schulverwalter.domain.usecase.InitializeSchulverwalterReauthUseCase
import plus.vplan.app.feature.schulverwalter.domain.usecase.UpdateSchulverwalterAccessUseCase

val schulverwalterModule = module {
    singleOf(::SchulverwalterRepositoryImpl).bind<SchulverwalterRepository>()
    singleOf(::InitializeSchulverwalterReauthUseCase)
    singleOf(::UpdateSchulverwalterAccessUseCase)
}