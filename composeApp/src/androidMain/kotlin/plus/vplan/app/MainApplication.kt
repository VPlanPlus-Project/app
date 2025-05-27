package plus.vplan.app

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.dsl.module
import plus.vplan.app.android.worker.SyncWorker
import plus.vplan.app.di.initKoin
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    companion object {
        const val POSTHOG_API_KEY = "phc_cS4RpGEmiGQJLvKLm5TFCZ4aEaqGRkWvWsOo7ko6pC6"
        const val POSTHOG_HOST = "https://eu.i.posthog.com"
    }

    override fun onCreate() {
        super.onCreate()

        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST
        ).apply {
            if (isDebug()) {
                flushAt = 1 // Flush events immediately for debugging
                flushIntervalSeconds = 1000 // Flush every second for debugging
            } else {
                flushAt = 10 // Flush events in batches of 100 in production
                flushIntervalSeconds = 60 // Flush every minute in production
            }
        }

        // Setup PostHog with the given Context and Config
        PostHogAndroid.setup(this, config)
        PostHog.register("\$app_build", BuildConfig.APP_VERSION_CODE)
        PostHog.register("\$os_name", "Android")
        PostHog.register("debug_mode", isDebug().toString())

        FirebaseApp.initializeApp(this)
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = !isDebug()

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