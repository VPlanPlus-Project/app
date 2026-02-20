package plus.vplan.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.data.repository.PlatformNotificationRepositoryImpl
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.ui.platform.RunBiometricAuthentication
import plus.vplan.app.ui.platform.RunBiometricAuthenticationImpl

actual val platformModule: Module = module {
    single<LocalFileRepository> { LocalFileRepositoryImpl() }
    single<PlatformNotificationRepository> { PlatformNotificationRepositoryImpl() }
    single<RunBiometricAuthentication> { RunBiometricAuthenticationImpl() }
}