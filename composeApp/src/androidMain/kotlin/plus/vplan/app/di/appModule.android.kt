package plus.vplan.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.data.source.database.VppDatabase

actual fun platformModule(): Module = module(createdAtStart = true) {
    single<VppDatabase>(createdAtStart = true) {
        Room.databaseBuilder<VppDatabase>(
            context = get(),
            name = get<Context>().getDatabasePath("data.db").absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .fallbackToDestructiveMigration(true)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .setQueryCoroutineContext(Dispatchers.Main)
            .build()
    }
}