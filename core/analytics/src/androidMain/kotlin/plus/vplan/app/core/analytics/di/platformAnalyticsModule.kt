package plus.vplan.app.core.analytics.di

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.analytics.AndroidAnalyticsRepository

actual val platformAnalyticsModule: Module = module(createdAtStart = true) {
    single {
        AndroidAnalyticsRepository(
            context = get(),
            isDebug = get(named("isDebug")),
            posthogApiKey = get(named("posthogApiKey")),
            versionCode = get(named("versionCode")),
        )
    }.bind<AnalyticsRepository>()
}
