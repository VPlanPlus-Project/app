package plus.vplan.app.feature.system.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import plus.vplan.app.feature.system.usecase.HandlePushNotificationUseCase

val systemModule = module {
    singleOf(::HandlePushNotificationUseCase)
}