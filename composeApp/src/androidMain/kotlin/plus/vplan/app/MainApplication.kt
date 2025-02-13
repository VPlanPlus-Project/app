package plus.vplan.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import plus.vplan.app.data.repository.LocalFileRepositoryImpl
import plus.vplan.app.di.initKoin
import plus.vplan.app.domain.repository.LocalFileRepository

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MainApplication)
            module {
                single { LocalFileRepositoryImpl(get()) }.bind<LocalFileRepository>()
            }
        }
    }
}