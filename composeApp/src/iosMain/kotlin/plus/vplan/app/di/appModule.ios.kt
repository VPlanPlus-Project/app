package plus.vplan.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.VppDatabaseConstructor
import plus.vplan.app.domain.repository.LocalFileRepository

actual fun platformModule(): Module = module(createdAtStart = true) {
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
    singleOf(::LocalFileRepositoryImpl).bind<LocalFileRepository>()
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