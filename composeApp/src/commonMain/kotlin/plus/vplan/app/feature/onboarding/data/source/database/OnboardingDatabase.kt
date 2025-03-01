package plus.vplan.app.feature.onboarding.data.source.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import plus.vplan.app.feature.onboarding.data.source.database.dao.OnboardingKeyValueDao
import plus.vplan.app.feature.onboarding.data.source.database.model.DbOnboardingGroup
import plus.vplan.app.feature.onboarding.data.source.database.model.DbOnboardingKeyValue

@Database(
    entities = [
        DbOnboardingKeyValue::class,
        DbOnboardingGroup::class
    ],
    version = 1
)
@ConstructedBy(OnboardingDatabaseCreator::class)
abstract class OnboardingDatabase : RoomDatabase() {
    abstract val keyValueDao: OnboardingKeyValueDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object OnboardingDatabaseCreator : RoomDatabaseConstructor<OnboardingDatabase> {
    override fun initialize(): OnboardingDatabase
}