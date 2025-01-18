package plus.vplan.app.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.data.repository.FileRepositoryImpl
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.repository.FileRepository

actual fun platformModule(): Module = module(createdAtStart = true) {
    single<VppDatabase>(createdAtStart = true) {
        Room.databaseBuilder<VppDatabase>(
            context = get(),
            name = get<Context>().getDatabasePath("data.db").absolutePath
        )
            .setDriver(AndroidSQLiteDriver())
            .fallbackToDestructiveMigration(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single<FileRepository> { FileRepositoryImpl(get()) }
}