package plus.vplan.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import plus.vplan.app.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
        }
    }
}