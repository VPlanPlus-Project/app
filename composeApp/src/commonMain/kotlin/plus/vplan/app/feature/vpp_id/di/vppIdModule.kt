package plus.vplan.app.feature.vpp_id.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import plus.vplan.app.feature.vpp_id.domain.usecase.AddVppIdUseCase
import plus.vplan.app.feature.vpp_id.ui.VppIdSetupViewModel

val vppIdModule = module {
    singleOf(::AddVppIdUseCase)
    viewModelOf(::VppIdSetupViewModel)
}