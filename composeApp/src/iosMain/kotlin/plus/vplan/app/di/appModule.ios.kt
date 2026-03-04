package plus.vplan.app.di

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import plus.vplan.app.core.data.file.FileOpener
import plus.vplan.app.core.data.file.ThumbnailGenerator
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.data.repository.PlatformNotificationRepositoryImpl
import plus.vplan.app.domain.model.OpenQuicklook
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.quicklook
import plus.vplan.app.ui.platform.RunBiometricAuthentication
import plus.vplan.app.ui.platform.RunBiometricAuthenticationImpl

@OptIn(ExperimentalForeignApi::class)
actual val platformModule: Module = module {
    single<LocalFileRepository> { LocalFileRepositoryImpl() }
    single<PlatformNotificationRepository> { PlatformNotificationRepositoryImpl() }
    single<RunBiometricAuthentication> { RunBiometricAuthenticationImpl() }
    
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