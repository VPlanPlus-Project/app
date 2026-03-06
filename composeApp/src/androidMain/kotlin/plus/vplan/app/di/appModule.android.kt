package plus.vplan.app.di

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import plus.vplan.app.AppBuildConfig
import plus.vplan.app.core.data.file.FileOpener
import plus.vplan.app.core.data.file.ThumbnailGenerator
import plus.vplan.app.core.platform.ActivityProvider
import plus.vplan.app.core.platform.AuthenticationRepository
import plus.vplan.app.core.platform.AuthenticationRepositoryImpl
import plus.vplan.app.core.platform.BiometricAuthentication
import plus.vplan.app.core.platform.BiometricAuthenticationImpl
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.platform.NotificationRepositoryImpl
import plus.vplan.app.ui.platform.OpenBiometricSettings
import plus.vplan.app.ui.platform.OpenBiometricSettingsImpl

actual val platformModule: Module = module(createdAtStart = true) {
    single<Boolean>(named("isDebug")) { AppBuildConfig.APP_DEBUG }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), getProperty("notification_small_icon", android.R.drawable.ic_dialog_info)) }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
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