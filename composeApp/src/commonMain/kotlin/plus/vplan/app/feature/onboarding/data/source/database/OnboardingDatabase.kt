package plus.vplan.app.feature.onboarding.data.source.database

import androidx.room.Database
import androidx.room.RoomDatabase
import plus.vplan.app.feature.onboarding.data.source.database.dao.OnboardingKeyValueDao
import plus.vplan.app.feature.onboarding.data.source.database.model.DbOnboardingKeyValue

@Database(
    entities = [
        DbOnboardingKeyValue::class
    ],
    version = 1
)
abstract class OnboardingDatabase : RoomDatabase() {
    abstract val keyValueDao: OnboardingKeyValueDao
}