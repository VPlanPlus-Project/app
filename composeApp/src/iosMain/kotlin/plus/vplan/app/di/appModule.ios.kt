package plus.vplan.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.data.repository.PlatformNotificationRepositoryImpl
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.VppDatabaseConstructor
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.ui.platform.RunBiometricAuthentication
import plus.vplan.app.ui.platform.RunBiometricAuthenticationImpl

actual val platformModule: Module = module {
    single<LocalFileRepository> { LocalFileRepositoryImpl() }
    single<PlatformNotificationRepository> { PlatformNotificationRepositoryImpl() }
    single<RunBiometricAuthentication> { RunBiometricAuthenticationImpl() }
    single<VppDatabase> {
        val dbFilePath = documentDirectory() + "/vpp.db"
        Room.databaseBuilder<VppDatabase>(
            name = dbFilePath,
            factory = { VppDatabaseConstructor.initialize() }
        )
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver()) // Very important
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )

    return requireNotNull(documentDirectory?.path)
}