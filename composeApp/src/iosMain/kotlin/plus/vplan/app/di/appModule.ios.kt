package plus.vplan.app.di

import dev.icerock.moko.permissions.PermissionsController
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import plus.vplan.app.core.data.file.FileOpener
import plus.vplan.app.core.data.file.OpenQuicklook
import plus.vplan.app.core.data.file.ThumbnailGenerator
import plus.vplan.app.core.platform.AuthenticationRepository
import plus.vplan.app.core.platform.AuthenticationRepositoryImpl
import plus.vplan.app.core.platform.BiometricAuthentication
import plus.vplan.app.core.platform.BiometricAuthenticationImpl
import plus.vplan.app.core.platform.NotificationRepository
import plus.vplan.app.core.platform.NotificationRepositoryImpl
import plus.vplan.app.core.platform.PermissionRepository
import plus.vplan.app.core.platform.PermissionRepositoryImpl
import plus.vplan.app.quicklook

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<NotificationRepository> { NotificationRepositoryImpl() }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl() }
    single<BiometricAuthentication> { BiometricAuthenticationImpl() }
    factory<PermissionRepository> { (controller: PermissionsController) -> PermissionRepositoryImpl(controller) }

    // New file infrastructure
    single<ThumbnailGenerator> { ThumbnailGenerator() }
    single<OpenQuicklook> { quicklook }
    single<FileOpener> { FileOpener(get()) }
    single<(String) -> String> {
        val fileManager = NSFileManager.defaultManager
        val documentDirectoryPath = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )?.path ?: throw IllegalStateException("Could not access document directory")

        val pathResolver: (String) -> String = { relativePath: String ->
            "$documentDirectoryPath/$relativePath"
        }
        pathResolver
    }
}