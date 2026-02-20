package plus.vplan.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.data.repository.PlatformAuthenticationRepositoryImpl
import plus.vplan.app.data.repository.PlatformNotificationImpl
import plus.vplan.app.domain.repository.ActivityProvider
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.PlatformAuthenticationRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.ui.platform.OpenBiometricSettings
import plus.vplan.app.ui.platform.OpenBiometricSettingsImpl
import plus.vplan.app.ui.platform.RunBiometricAuthentication
import plus.vplan.app.ui.platform.RunBiometricAuthenticationImpl

actual val platformModule: Module = module(createdAtStart = true) {
    single<LocalFileRepository> { LocalFileRepositoryImpl(get()) }
    single<PlatformNotificationRepository> { PlatformNotificationImpl(get(), getProperty("notification_small_icon", android.R.drawable.ic_dialog_info)) }
    single<PlatformAuthenticationRepository> { PlatformAuthenticationRepositoryImpl(get()) }
    single<OpenBiometricSettings> { OpenBiometricSettingsImpl(get()) }
    single<RunBiometricAuthentication> { RunBiometricAuthenticationImpl() }
    single<ActivityProvider> { getProperty<ActivityProvider>("activity_provider") }
}