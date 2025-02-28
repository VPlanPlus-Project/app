package plus.vplan.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.android.worker.SyncWorker
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.di.initKoin
import plus.vplan.app.domain.repository.LocalFileRepository
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
            workManagerFactory()
            module {
                single { WorkManager.getInstance(androidContext()) }
                workerOf(::SyncWorker)
            }
        }

        PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
            .let { request ->
                WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                    "SyncWorker",
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            }
    }
}