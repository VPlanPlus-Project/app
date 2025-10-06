package plus.vplan.app.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.LOG_DATABASE_QUERIES
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.data.repository.PlatformAuthenticationRepositoryImpl
import plus.vplan.app.data.repository.PlatformNotificationImpl
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.domain.repository.LocalFileRepository
import plus.vplan.app.domain.repository.PlatformAuthenticationRepository
import plus.vplan.app.domain.repository.PlatformNotificationRepository
import plus.vplan.app.ui.platform.OpenBiometricSettings
import plus.vplan.app.ui.platform.OpenBiometricSettingsImpl
import plus.vplan.app.ui.platform.RunBiometricAuthentication
import plus.vplan.app.ui.platform.RunBiometricAuthenticationImpl
import java.util.concurrent.Executors

actual val platformModule: Module = module(createdAtStart = true) {
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
    single<LocalFileRepository> { LocalFileRepositoryImpl(get()) }
    single<PlatformNotificationRepository> { PlatformNotificationImpl(get()) }
    single<PlatformAuthenticationRepository> { PlatformAuthenticationRepositoryImpl(get()) }
    single<OpenBiometricSettings> { OpenBiometricSettingsImpl(get()) }
    single<RunBiometricAuthentication> { RunBiometricAuthenticationImpl() }
}