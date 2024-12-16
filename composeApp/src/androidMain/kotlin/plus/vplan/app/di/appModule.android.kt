package plus.vplan.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.converters.IdOriginConverter
import plus.vplan.app.data.source.database.converters.UuidTypeConverter

actual fun platformModule(): Module = module(createdAtStart = true) {
    single<VppDatabase>(createdAtStart = true) {
        Room.databaseBuilder<VppDatabase>(
            context = get(),
            name = get<Context>().getDatabasePath("data.db").absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .enableMultiInstanceInvalidation()
            .setQueryCoroutineContext(Dispatchers.IO)
            .addTypeConverter(UuidTypeConverter())
            .addTypeConverter(IdOriginConverter())
            .build()
    }
}