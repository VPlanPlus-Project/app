package plus.vplan.app.feature.onboarding.data.source.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "onboarding_key_value",
    primaryKeys = ["key"]
)
data class DbOnboardingKeyValue(
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String
)
