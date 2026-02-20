package plus.vplan.app.core.database.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.core.database.VppDatabase
import java.util.concurrent.Executors

/**
 * Log every SQL transaction with its queries and parameters. See the implementation of the Room
 * Builder.
 */
const val LOG_DATABASE_QUERIES: Boolean = false

actual val databaseModule: Module = module {
    single<VppDatabase>(createdAtStart = true) {
        Room.databaseBuilder<VppDatabase>(
            context = get(),
            name = get<Context>().getDatabasePath("data.db").absolutePath
        )
            .let { builder ->
                if (LOG_DATABASE_QUERIES) builder.setQueryCallback({ sqlQuery, bindArgs ->
                    Log.d("RoomDatabase", "SQL: $sqlQuery | args: $bindArgs")
                }, Executors.newSingleThreadExecutor())
                else builder
            }
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .fallbackToDestructiveMigration(false)
            .config()
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}