package plus.vplan.app.feature.sync.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.sync.domain.usecase.UpdateWeeksUseCase

val syncModule = module {
    singleOf(::UpdateWeeksUseCase)
}