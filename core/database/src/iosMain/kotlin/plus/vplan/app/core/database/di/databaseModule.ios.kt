@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.database.di

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.VppDatabaseConstructor

actual val roomModule: Module = module {
    single<VppDatabase> {
        val dbFilePath = documentDirectory() + "/vpp.db"
        Room.databaseBuilder<VppDatabase>(
            name = dbFilePath,
            factory = { VppDatabaseConstructor.initialize() }
        )
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(NonEvictingDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver()))
            .setQueryCoroutineContext(newSingleThreadContext("RoomQuery"))
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