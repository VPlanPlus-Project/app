package plus.vplan.app.feature.onboarding.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.feature.onboarding.data.source.database.OnboardingDatabase

actual fun onboardingDatabaseModule(): Module = module {
    single<OnboardingDatabase> {
        Room.inMemoryDatabaseBuilder<OnboardingDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}