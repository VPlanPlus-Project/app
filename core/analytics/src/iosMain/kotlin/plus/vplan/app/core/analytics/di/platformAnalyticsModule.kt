package plus.vplan.app.core.analytics.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.analytics.IosAnalyticsRepository

actual val platformAnalyticsModule: Module = module {
    singleOf(::IosAnalyticsRepository).bind<AnalyticsRepository>()
}
