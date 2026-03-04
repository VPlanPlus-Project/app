package plus.vplan.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.core.data.file.FileOpener
import plus.vplan.app.core.data.file.ThumbnailGenerator
import plus.vplan.app.core.platform.ActivityProvider
import plus.vplan.app.core.platform.BiometricAuthentication
import plus.vplan.app.core.platform.BiometricAuthenticationImpl
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.platform.NotificationRepositoryImpl
import plus.vplan.app.data.repository.PlatformAuthenticationRepositoryImpl
import plus.vplan.app.domain.repository.PlatformAuthenticationRepository
import plus.vplan.app.ui.platform.OpenBiometricSettings
import plus.vplan.app.ui.platform.OpenBiometricSettingsImpl

actual val platformModule: Module = module(createdAtStart = true) {
    single<NotificationRepository> { NotificationRepositoryImpl(get(), getProperty("notification_small_icon", android.R.drawable.ic_dialog_info)) }
    single<PlatformAuthenticationRepository> { PlatformAuthenticationRepositoryImpl(get()) }
    single<OpenBiometricSettings> { OpenBiometricSettingsImpl(get()) }
    single<BiometricAuthentication> { BiometricAuthenticationImpl() }
    single<ActivityProvider> { getProperty<ActivityProvider>("activity_provider") }
    
    // New file infrastructure
    single<ThumbnailGenerator> { 
        ThumbnailGenerator(get<android.content.Context>().filesDir)
    }
    single<FileOpener> { 
        FileOpener(get<android.content.Context>()) 
    }
    single<(String) -> String> { 
        val filesDir = get<android.content.Context>().filesDir.absolutePath
        { relativePath: String -> "$filesDir/$relativePath" }
    }
}