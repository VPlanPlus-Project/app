package plus.vplan.app.di

import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.core.data.file.FileOpener
import plus.vplan.app.core.data.file.ThumbnailGenerator
import plus.vplan.app.core.platform.ActivityProvider
import plus.vplan.app.core.platform.AuthenticationRepository
import plus.vplan.app.core.platform.AuthenticationRepositoryImpl
import plus.vplan.app.core.platform.BiometricAuthentication
import plus.vplan.app.core.platform.BiometricAuthenticationImpl
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.platform.NotificationRepositoryImpl
import plus.vplan.app.core.platform.PermissionRepository
import plus.vplan.app.core.platform.PermissionRepositoryImpl
import plus.vplan.app.core.platform.PlatformRepository
import plus.vplan.app.core.platform.PlatformRepositoryImpl
import plus.vplan.app.ui.platform.OpenBiometricSettings
import plus.vplan.app.ui.platform.OpenBiometricSettingsImpl

actual val platformModule: Module = module(createdAtStart = true) {
    single<NotificationRepository> { NotificationRepositoryImpl(get(), getProperty("notification_small_icon", android.R.drawable.ic_dialog_info)) }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }
    single<OpenBiometricSettings> { OpenBiometricSettingsImpl(get()) }
    single<BiometricAuthentication> { BiometricAuthenticationImpl() }
    single<ActivityProvider> { getProperty<ActivityProvider>("activity_provider") }
    factory<PermissionRepository> { (controller: PermissionsController) -> PermissionRepositoryImpl(controller) }
    single<PlatformRepository> { PlatformRepositoryImpl() }

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