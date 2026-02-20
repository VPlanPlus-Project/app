package plus.vplan.app.core.database.di

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.VppDatabaseConstructor

actual val databaseModule: Module = module {
    single<VppDatabase> {
        val dbFilePath = documentDirectory() + "/vpp.db"
        Room.databaseBuilder<VppDatabase>(
            name = dbFilePath,
            factory = { VppDatabaseConstructor.initialize() }
        )
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver()) // Very important
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