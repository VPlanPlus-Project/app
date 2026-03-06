package plus.vplan.app.core.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module

val analyticsModule = module {
    includes(platformAnalyticsModule)
}

expect val platformAnalyticsModule: Module
