package plus.vplan.app.feature.onboarding.di

import android.content.Context
import androidx.room.Room
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.feature.onboarding.data.source.database.OnboardingDatabase

private fun buildDatabase(context: Context): OnboardingDatabase {
    return Room.inMemoryDatabaseBuilder<OnboardingDatabase>(
        context = context
    )
        .build()
}
actual fun onboardingDatabaseModule(): Module = module {
    single<OnboardingDatabase> { buildDatabase(get()) }
}