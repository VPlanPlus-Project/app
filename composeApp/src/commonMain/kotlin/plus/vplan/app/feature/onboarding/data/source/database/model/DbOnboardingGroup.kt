package plus.vplan.app.feature.onboarding.data.source.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "onboarding_group",
    primaryKeys = ["id"]
)
data class DbOnboardingGroup(
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "users") val users: Int
)